package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.DeploymentInfo;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.cycle.exception.ErrorCodes;
import com.tesco.pma.cycle.exception.PMCycleException;
import com.tesco.pma.cycle.model.PMProcessModelParser;
import com.tesco.pma.cycle.model.ResourceProvider;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.service.PMProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.COMPLETED;
import static com.tesco.pma.cycle.api.PMCycleStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMCycleStatus.FAILED;
import static com.tesco.pma.cycle.api.PMCycleStatus.INACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.REGISTERED;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_ALREADY_EXISTS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_ALLOWED_TO_START;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_COLLEAGUE;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static java.util.Set.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class PMCycleServiceImpl implements PMCycleService {

    public static final String ENTRY_CONFIG_KEY_CONDITION = "entry-config-key";
    public static final String STATUS_CONDITION = "status";
    public static final String TEMPLATE_UUID_CONDITION = "template-uuid";
    private final PMCycleDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessManagerService processManagerService;
    private final PMProcessService pmProcessService;
    private final FileService fileService;
    private final ResourceProvider resourceProvider;

    private static final String ORG_KEY_PARAMETER_NAME = "organisationKey";
    private static final String TEMPLATE_UUID_PARAMETER_NAME = "templateUUID";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String CYCLE_STATUSES_PARAMETER_NAME = "statuses";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String INCLUDE_METADATA_PARAMETER_NAME = "includeMetadata";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROCESS_NAME_PARAMETER_NAME = "processName";
    private static final String CONDITION_PARAMETER_NAME = "condition";

    private static final Map<PMCycleStatus, DictionaryFilter<PMCycleStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                ACTIVE, includeFilter(of(INACTIVE, DRAFT, REGISTERED)),
                INACTIVE, includeFilter(of(REGISTERED, DRAFT)),
                DRAFT, includeFilter(of(DRAFT)),
                COMPLETED, includeFilter(of(ACTIVE, DRAFT))
        );
    }

    @Override
    @Transactional
    public PMCycle create(@NotNull PMCycle cycle, UUID loggedUserUUID) {
        return intCreate(cycle, loggedUserUUID);
    }

    @Override
    @Transactional
    public PMCycle publish(@NotNull PMCycle cycle, UUID loggedUserUUID) {
        log.debug("Request to publish Performance cycle : {}", cycle);

        var cycleUuid = cycle.getUuid();
        if (cycleUuid == null) {
            intCreate(cycle, loggedUserUUID);
        } else {
            intUpdate(cycle);
        }

        String processId = intDeploy(cycle);
        log.debug("Process definition id: {}", processId);
        intStartProcess(cycle, processId);

        return cycle;
    }

    @Override
    @Transactional
    public PMCycle updateStatus(UUID uuid, PMCycleStatus status) {
        return intUpdateStatus(uuid, status);
    }

    @Override
    @Transactional(readOnly = true)
    public PMCycle get(UUID uuid) {
        var pmCycle = cycleDAO.read(uuid, null);
        if (null == pmCycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }
        return pmCycle;
    }

    @Override
    @Transactional
    public PMCycle update(PMCycle cycle) {
        return intUpdate(cycle);
    }

    @Override
    @Transactional(readOnly = true)
    public PMCycle getCurrentByColleague(UUID colleagueUuid) {
        var activeFilter = DictionaryFilter.includeFilter(Set.of(ACTIVE));
        var cycles = cycleDAO.getByColleague(colleagueUuid, activeFilter);
        if (null == cycles || cycles.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return cycles.iterator().next();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PMCycle> getByColleague(UUID colleagueUuid) {
        var cycles = cycleDAO.getByColleague(colleagueUuid);
        if (null == cycles || cycles.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return cycles;
    }

    @Override
    public List<PMCycle> getAll(boolean includeMetadata) {
        //TODO add requestQuery to endpoint+frontend+handlers
        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setFilters(Collections.emptyList());
        var results = cycleDAO.getAll(requestQuery, includeMetadata);
        if (null == results) {
            throw notFound(PM_CYCLE_NOT_FOUND,
                    Map.of(INCLUDE_METADATA_PARAMETER_NAME, includeMetadata));
        }
        return results;
    }

    @Override
    public PMCycleMetadata getMetadata(UUID fileUuid) {
        var file = fileService.get(fileUuid, true);
        var model = Bpmn.readModelFromStream(new ByteArrayInputStream(file.getFileContent()));
        var parser = new PMProcessModelParser(resourceProvider, messageSourceAccessor);
        return parser.parse(model);
    }

    @Override
    @Transactional
    public String deploy(PMCycle cycle) {
        return intDeploy(cycle);
    }

    @Override
    @Transactional
    public void start(UUID cycleUUID, String processId) {
        DictionaryFilter<PMCycleStatus> statusFilter = includeFilter(REGISTERED);
        var cycle = cycleDAO.read(cycleUUID, statusFilter);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUUID,
                            CYCLE_STATUSES_PARAMETER_NAME, statusFilter.getItems()));
        }
        intStartProcess(cycle, processId);
    }

    @Override
    public void updateJsonMetadata(UUID uuid, String metadata) {
        cycleDAO.updateMetadata(uuid, metadata);
    }

    private String cycleFailed(String processKey, UUID uuid, Exception ex) {
        log.error("Performance cycle publish error, cause: ", ex);
        try {
            intUpdateStatus(uuid, FAILED);
        } catch (NotFoundException exc) {
            log.error("Performance cycle change status error, cause: ", exc);
        }
        throw pmCycleException(ErrorCodes.PROCESS_EXECUTION_EXCEPTION, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid,//NOPMD
                PROCESS_NAME_PARAMETER_NAME, processKey));
    }

    //TODO refactor to common solution (include @com.tesco.pma.review.service.ReviewServiceImpl)
    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return notFound(errorCode, params, null);
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params, Throwable cause) {
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }

    private DatabaseConstraintViolationException databaseConstraintViolation(ErrorCodeAware errorCode,
                                                                             Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }


    private PMCycle intUpdateStatus(UUID uuid, PMCycleStatus status) {
        var cycle = cycleDAO.read(uuid, null);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        cycle.setStatus(status);
        DictionaryFilter<PMCycleStatus> statusFilter = UPDATE_STATUS_RULE_MAP.get(status);

        if (1 == cycleDAO.updateStatus(uuid, status, statusFilter)) {
            log.debug("Performance cycle UUID: {} changed status to: {}", cycle.getUuid(), status);
            return cycle;
        } else {
            throw notFound(PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            PREV_STATUSES_PARAMETER_NAME, statusFilter));
        }
    }

    private PMCycleException pmCycleException(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new PMCycleException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, null);
    }


    private String intDeployProcess(UUID templateUuid, String processName) throws Exception {

        var file = fileService.get(templateUuid, true);
        InputStream fileContent = new ByteArrayInputStream(file.getFileContent());

        String resourceName = file.getFileName();

        DeploymentInfo deploymentInfo = processManagerService.deploy(processName,
                Map.of(resourceName, fileContent));
        log.debug("Deployment id: {}", deploymentInfo.getId());


        List<String> procdefs = processManagerService.getProcessesIds(deploymentInfo.getId(), resourceName);

        return procdefs.iterator().next();
    }

    private PMCycle intCreate(PMCycle cycle, UUID loggedUserUUID) {
        log.debug("Request to create performance cycle : {}, by user UUID: {}", cycle, loggedUserUUID);

        try {
            cycle.setUuid(UUID.randomUUID());
            cycle.setStatus(DRAFT);
            cycle.setCreatedBy(ColleagueSimple
                    .builder()
                    .uuid(loggedUserUUID)
                    .build());

            cycleDAO.create(cycle);
            log.debug("Performance cycle createdUUID: {}", cycle.getUuid());
            return cycle;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    PM_CYCLE_ALREADY_EXISTS,
                    Map.of(ORG_KEY_PARAMETER_NAME, cycle.getEntryConfigKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, cycle.getTemplateUUID()), e);
        }
    }


    private PMCycle intUpdate(PMCycle cycle) {
        var statusFilter = UPDATE_STATUS_RULE_MAP.get(cycle.getStatus());
        if (1 != cycleDAO.update(cycle, statusFilter)) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycle.getUuid(),
                            CYCLE_STATUSES_PARAMETER_NAME, statusFilter.getItems()));
        }
        return cycle;
    }


    private String intDeploy(PMCycle cycle) {
        String processKey = cycle.getMetadata().getCycle().getCode();
        UUID uuid = cycle.getUuid();

        if (null == processKey || processKey.isEmpty()) {
            throw pmCycleException(ErrorCodes.PROCESS_NAME_IS_EMPTY, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        try {
            var processId = intDeployProcess(cycle.getTemplateUUID(), processKey);
            log.debug("Process definition id: {}", processId);
            intUpdateStatus(uuid, REGISTERED);
            return processId;
        } catch (Exception e) {
            return cycleFailed(processKey, uuid, e);
        }
    }


    private void intStartProcess(PMCycle cycle, String processId) {

        validateCycleForStart(cycle);

        var cycleUUID = cycle.getUuid();
        try {
            var processUUID = processManagerService.runProcessById(processId, prepareFlowProperties(cycle));
            log.debug("Started process: {}", processUUID);

            var pmRuntimeProcess = PMRuntimeProcess.builder()
                    .bpmProcessId(processId)
                    .cycleUuid(cycleUUID)
                    .businessKey(cycle.getEntryConfigKey())
                    .build();

            pmRuntimeProcess = pmProcessService.register(pmRuntimeProcess, PMProcessStatus.STARTED);
            log.debug("Started PM process: {}", pmRuntimeProcess);
            intUpdateStatus(cycleUUID, ACTIVE);
        } catch (ProcessExecutionException e) {
            cycleFailed(processId, cycleUUID, e);
        }
    }

    private Map<String, Object> prepareFlowProperties(PMCycle cycle) {
        Map<String, Object> props = new HashMap<>();
        props.put(TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());
        props.put(FlowParameters.PM_CYCLE.name(), cycle);
        return props;
    }

    private void validateCycleForStart(PMCycle cycle) {
        RequestQuery query = new RequestQuery();

        query.setFilters(List.of(
                new Condition(ENTRY_CONFIG_KEY_CONDITION, EQUALS, cycle.getEntryConfigKey()),
                new Condition(STATUS_CONDITION, EQUALS, ACTIVE.getId()),
                new Condition(TEMPLATE_UUID_CONDITION, EQUALS, cycle.getTemplateUUID())
        ));

        List<PMCycle> cycleList = cycleDAO.getAll(query, false);
        if (null != cycleList) {
            throw notFound(PM_CYCLE_NOT_ALLOWED_TO_START,
                    Map.of(CONDITION_PARAMETER_NAME, query.getFilters()));
        }

    }
}
