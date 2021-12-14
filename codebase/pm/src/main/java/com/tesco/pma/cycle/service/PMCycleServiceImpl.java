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
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.logging.TraceUtils;
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
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_ALREADY_EXISTS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_COLLEAGUE;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static java.util.Set.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class PMCycleServiceImpl implements PMCycleService {

    private final PMCycleDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessManagerService processManagerService;
    private final PMProcessService pmProcessService;
    private final FileService fileService;
    private final ResourceProvider resourceProvider;

    private static final String ORG_KEY_PARAMETER_NAME = "organisationKey";
    private static final String TEMPLATE_UUID_PARAMETER_NAME = "templateUUID";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String INCLUDE_METADATA_PARAMETER_NAME = "includeMetadata";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROCESS_NAME_PARAMETER_NAME = "processName";

    private static final Map<PMCycleStatus, DictionaryFilter<PMCycleStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                ACTIVE, includeFilter(of(INACTIVE, DRAFT)),
                INACTIVE, includeFilter(of(ACTIVE, DRAFT)),
                DRAFT, includeFilter(of(ACTIVE, INACTIVE)),
                COMPLETED, includeFilter(of(ACTIVE, INACTIVE, DRAFT))
        );
    }

    @Override
    @Transactional
    public PMCycle create(@NotNull PMCycle cycle, String loggedUserName) {
        return intCreateOrUpdateCycle(cycle, loggedUserName);
    }


    @Override
    @Transactional
    public PMCycle publish(@NotNull PMCycle cycle, String loggedUserName) {
        log.debug("Request to publish Performance cycle : {}", cycle);

        UUID cycleUuid = intCreateOrUpdateCycle(cycle, loggedUserName).getUuid();
        String processName = cycle.getMetadata().getCycle().getCode();

        if (null == processName || processName.isEmpty()) {
            throw pmCycleException(ErrorCodes.PROCESS_NAME_IS_EMPTY, Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUuid));
        }

        try {

            String processId = intDeployProcess(cycle.getTemplateUUID(), processName);
            log.debug("Process definition id: {}", processId);

            Map<String, String> props = cycle.getProperties() != null ? cycle.getProperties().getMapJson() : Collections.emptyMap();

            props.put(TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());

            var processUUID = processManagerService.runProcessById(processId, props);
            log.debug("Started process: {}", processUUID);

            var pmRuntimeProcess = PMRuntimeProcess.builder()
                    .bpmProcessId(processId)
                    .cycleUuid(cycleUuid)
                    .businessKey(cycle.getEntryConfigKey())
                    .build();

            pmRuntimeProcess = pmProcessService.register(pmRuntimeProcess, PMProcessStatus.STARTED);
            log.debug("Started PM process: {}", pmRuntimeProcess);

            intUpdateStatus(cycleUuid, ACTIVE);
        } catch (Exception e) {
            cycleFailed(processName, cycleUuid, e);
        }

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
        var pmCycleStatusDictionaryFilter = UPDATE_STATUS_RULE_MAP.get(cycle.getStatus());
        if (1 != cycleDAO.update(cycle, pmCycleStatusDictionaryFilter)) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycle.getUuid()));
        }
        return cycle;
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
        var results = cycleDAO.getAll(includeMetadata);
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
    public String deploy(PMCycle cycle) {
        String processName = cycle.getMetadata().getCycle().getCode();
        UUID uuid = cycle.getUuid();

        if (null == processName || processName.isEmpty()) {
            throw pmCycleException(ErrorCodes.PROCESS_NAME_IS_EMPTY, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        try {
            var processId = intDeployProcess(cycle.getTemplateUUID(), processName);
            log.debug("Process definition id: {}", processId);
            return processId;
        } catch (Exception e) {
            return cycleFailed(processName, uuid, e);
        }
    }


    @Override
    public void start(UUID cycleUUID, String processId) {
        var cycle = cycleDAO.read(cycleUUID, DictionaryFilter.includeFilter(DRAFT));
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUUID));
        }

        try {
            Map<String, String> props = cycle.getProperties() != null ? cycle.getProperties().getMapJson() : Collections.emptyMap();

            props.put(TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());
            var processUUID = processManagerService.runProcessById(processId, props);
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

    @Override
    public void updateJsonMetadata(UUID uuid, String metadata) {
        cycleDAO.updateMetadata(uuid, metadata);
    }

    private String cycleFailed(String processName, UUID uuid, Exception ex) {
        log.error("Performance cycle publish error, cause: ", ex);
        try {
            intUpdateStatus(uuid, FAILED);
        } catch (NotFoundException exc) {
            log.error("Performance cycle change status error, cause: ", exc);
        }
        throw pmCycleException(ErrorCodes.PROCESS_EXECUTION_EXCEPTION, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid,//NOPMD
                PROCESS_NAME_PARAMETER_NAME, processName));
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


    private PMCycle intCreateOrUpdateCycle(PMCycle cycle, String loggedUserName) {
        log.debug("Request to create or update Performance cycle : {}", cycle);
        if (cycle.getUuid() == null) {
            cycle.setUuid(UUID.randomUUID());
            cycle.setStatus(DRAFT);
            cycle.setCreatedBy(ColleagueSimple
                    .builder()
                    .uuid(UUID.fromString(loggedUserName))
                    .build());
        }

        try {
            if (1 != cycleDAO.createOrUpdate(cycle, DictionaryFilter.includeFilter(DRAFT))) {
                throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                        Map.of(CYCLE_UUID_PARAMETER_NAME, cycle.getUuid()));
            }
            log.debug("Performance cycle created/updated UUID: {}", cycle.getUuid());
            return cycle;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    PM_CYCLE_ALREADY_EXISTS,
                    Map.of(ORG_KEY_PARAMETER_NAME, cycle.getEntryConfigKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, cycle.getTemplateUUID()), e);
        }
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
}
