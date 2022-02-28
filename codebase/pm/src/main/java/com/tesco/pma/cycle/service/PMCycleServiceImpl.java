package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.CompositePMCycleMetadataResponse;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.api.PMForm;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.api.request.PMCycleUpdateFormRequest;
import com.tesco.pma.cycle.api.request.PMFormRequest;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.cycle.exception.ErrorCodes;
import com.tesco.pma.cycle.exception.PMCycleException;
import com.tesco.pma.cycle.api.model.PMProcessModelParser;
import com.tesco.pma.util.ResourceProvider;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.service.PMProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.COMPLETED;
import static com.tesco.pma.cycle.api.PMCycleStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMCycleStatus.FAILED;
import static com.tesco.pma.cycle.api.PMCycleStatus.INACTIVE;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_METADATA_NOT_FOUND;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_ALLOWED_TO_START;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_COLLEAGUE;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE;
import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.process.api.PMProcessStatus.STARTED;
import static com.tesco.pma.util.FileUtils.getFormName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Set.of;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class PMCycleServiceImpl implements PMCycleService {

    public static final String ENTRY_CONFIG_KEY_CONDITION = "entry-config-key";
    public static final String STATUS_CONDITION = "status";
    public static final String TEMPLATE_UUID_CONDITION = "template-uuid";
    private static final String CYCLE_UUID = "cycleUUID";
    private static final String STATUS_FILTER = "status_filter";
    private final PMCycleDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessManagerService processManagerService;
    private final PMProcessService pmProcessService;
    private final FileService fileService;
    private final ResourceProvider resourceProvider;
    private final DeploymentService deploymentService;

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
                ACTIVE, includeFilter(of(INACTIVE, DRAFT, PMCycleStatus.REGISTERED)),
                INACTIVE, includeFilter(of(PMCycleStatus.REGISTERED, DRAFT)),
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
    public PMCycle publish(@NotNull PMCycle cycle, UUID loggedUserUUID) {
        log.debug("Request to publish Performance cycle : {}", cycle);

        if (cycle.getUuid() == null) {
            intCreate(cycle, loggedUserUUID);
        } else {
            intUpdate(cycle);
        }

        UUID rtProcessUuid = intDeploy(cycle.getUuid());
        log.debug("Runtime process uuid: {}", rtProcessUuid);
        intStartCycle(cycle.getUuid());

        return cycle;
    }

    @Override
    @Transactional
    public PMCycle updateStatus(UUID uuid, PMCycleStatus status) {
        return intUpdateStatus(uuid, status, null); // todo move status map to BPMN or DMN
    }

    @Override
    @Transactional
    public PMCycle updateStatus(UUID uuid, PMCycleStatus status, DictionaryFilter<PMCycleStatus> statusFilter) {
        return intUpdateStatus(uuid, status, statusFilter);
    }

    @Override
    public CompositePMCycleResponse get(UUID uuid, boolean includeForms) {
        var pmCycle = cycleDAO.read(uuid, null);
        if (null == pmCycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }
        var compositeCycle = new CompositePMCycleResponse();
        compositeCycle.setCycle(pmCycle);

        if (includeForms) {
            compositeCycle.setForms(getFormsForCycleMetadata(pmCycle.getMetadata()));
        }

        return compositeCycle;
    }

    @Override
    @Transactional
    public PMCycle update(PMCycle cycle) {
        return intUpdate(cycle);
    }

    @Override
    public PMCycle getCurrentByColleague(UUID colleagueUuid) {
        var activeFilter = includeFilter(Set.of(ACTIVE));
        var cycles = cycleDAO.getByColleague(colleagueUuid, activeFilter);
        if (null == cycles || cycles.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return cycles.iterator().next();
    }

    @Override
    public CompositePMCycleMetadataResponse getCurrentMetadataByColleague(@NotNull UUID colleagueUuid, boolean includeForms) {
        var currentCycle = getCurrentByColleague(colleagueUuid);

        if (null == currentCycle.getMetadata()) {
            throw new NotFoundException(PM_CYCLE_METADATA_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PM_CYCLE_METADATA_NOT_FOUND,
                            Map.of(CYCLE_UUID_PARAMETER_NAME, currentCycle.getUuid(),
                                    COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid)));

        }

        var compositeMetadata = new CompositePMCycleMetadataResponse();
        compositeMetadata.setMetadata(currentCycle.getMetadata());

        if (includeForms) {
            compositeMetadata.setForms(getFormsForCycleMetadata(currentCycle.getMetadata()));
        }

        return compositeMetadata;
    }

    @Override
    public List<PMCycle> getByColleague(UUID colleagueUuid) {
        var cycles = cycleDAO.getByColleague(colleagueUuid);
        if (null == cycles || cycles.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return cycles;
    }

    @Override
    public List<PMCycle> findAll(RequestQuery requestQuery, boolean includeMetadata) {
        return cycleDAO.findAll(requestQuery, includeMetadata);
    }

    @Override
    public CompositePMCycleMetadataResponse getFileMetadata(UUID fileUuid, boolean includeForms) {

        var file = fileService.get(fileUuid, true);
        var model = Bpmn.readModelFromStream(new ByteArrayInputStream(file.getFileContent()));
        var metadata = new PMProcessModelParser(resourceProvider, messageSourceAccessor).parse(model);
        var compositeMetadata = new CompositePMCycleMetadataResponse();

        compositeMetadata.setMetadata(metadata);
        if (includeForms) {
            compositeMetadata.setForms(getFormsForCycleMetadata(metadata));
        }
        return compositeMetadata;
    }

    @Override
    @Transactional
    public UUID deploy(UUID uuid) {
        return intDeploy(uuid);
    }

    @Override
    @Transactional
    public void start(UUID uuid) {
        //TODO get rt
        intStartCycle(uuid);
    }

    @Override
    @Transactional
    public void completeCycle(UUID cycleUUID) {
        intUpdateStatus(cycleUUID, COMPLETED, null); // todo move status map to BPMN or DMN
        //TODO update rt process
    }

    @Override
    @Transactional
    public PMCycle updateForm(UUID cycleUuid, PMCycleUpdateFormRequest updateFormRequest) {
        log.debug("Updating form for cycle:{}, updateFormRequest:{}", cycleUuid, updateFormRequest);

        PMCycle cycle = cycleDAO.read(cycleUuid, null);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUuid));
        }

        PMForm pmFormFrom = getPMForm(updateFormRequest.getChangeFrom());
        PMForm pmFormTo = getPMForm(updateFormRequest.getChangeTo());

        cycle.getMetadata().getCycle().getTimelinePoints().stream()
                .filter(tpe -> REVIEW == tpe.getType())
                .map(PMReviewElement.class::cast)
                .filter(rw -> rw.getForm() != null && rw.getForm().getId().equals(pmFormFrom.getId()))
                .findAny()
                .orElseThrow(() -> new NotFoundException(ERROR_FILE_NOT_FOUND.name(),
                        "Form was not found for changing", pmFormFrom.getKey()))
                .setForm(pmFormTo);

        cycleDAO.update(cycle, null);
        return cycle;
    }

    @Override
    @Transactional
    public PMCycle updateFormToLatestVersion(UUID cycleUuid, String formKey) {
        log.debug("Updating form to the latest version for the cycle:{}, formKey:{}", cycleUuid, formKey);

        PMCycle cycle = cycleDAO.read(cycleUuid, null);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUuid));
        }

        PMForm latestForm = getPMForm(formKey);

        cycle.getMetadata().getCycle().getTimelinePoints().stream()
                .filter(tpe -> tpe.getType() == REVIEW)
                .map(review -> (PMReviewElement) review)
                .filter(rw -> rw.getForm() != null && rw.getForm().getKey().equals(formKey))
                .findAny()
                .orElseThrow(() -> new NotFoundException(ERROR_FILE_NOT_FOUND.name(),
                        "Form was not found for changing", formKey))
                .setForm(latestForm);

        cycleDAO.update(cycle, null);
        return cycle;
    }

    private void cycleFailed(String processKey, UUID uuid, Exception ex) {
        log.error("Performance cycle publish error, cause: ", ex);
        try {
            intUpdateStatus(uuid, FAILED, null); // todo move status map to BPMN or DMN
        } catch (NotFoundException exc) {
            log.error("Performance cycle change status error, cause: ", exc);
        }
        throw pmCycleException(ErrorCodes.PROCESS_EXECUTION_EXCEPTION, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid,//NOPMD
                PROCESS_NAME_PARAMETER_NAME, processKey), ex);
    }

    //TODO refactor to common solution (include @com.tesco.pma.review.service.ReviewServiceImpl)
    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, null);
    }

    private DatabaseConstraintViolationException databaseConstraintViolation(Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(((ErrorCodeAware) ErrorCodes.PM_CYCLE_ALREADY_EXISTS).getCode(),
                messageSourceAccessor.getMessage(((ErrorCodeAware) ErrorCodes.PM_CYCLE_ALREADY_EXISTS).getCode(), params), null, cause);
    }


    private PMCycle intUpdateStatus(UUID uuid, PMCycleStatus status, DictionaryFilter<PMCycleStatus> statusFilter) {
        var cycle = cycleDAO.read(uuid, null);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        var resultStatusFilter = statusFilter == null || statusFilter.isEmpty()
                ? UPDATE_STATUS_RULE_MAP.get(status) // todo move status map to BPMN or DMN
                : statusFilter;

        if (1 == cycleDAO.updateStatus(uuid, status, resultStatusFilter)) {
            cycle.setStatus(status);
            log.debug("Performance cycle UUID: {} changed status to: {}", cycle.getUuid(), status);
            return cycle;
        } else {
            throw notFound(PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            PREV_STATUSES_PARAMETER_NAME, resultStatusFilter));
        }
    }

    private PMCycleException pmCycleException(ErrorCodeAware errorCode, Map<String, ?> params, Throwable cause) {
        return new PMCycleException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
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
                    Map.of(ORG_KEY_PARAMETER_NAME, cycle.getEntryConfigKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, cycle.getTemplate().getUuid()), e);
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


    private UUID intDeploy(UUID uuid) {
        DictionaryFilter<PMCycleStatus> statusFilter = includeFilter(DRAFT);
        var cycle = cycleDAO.read(uuid, statusFilter);

        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid,
                            CYCLE_STATUSES_PARAMETER_NAME, statusFilter.getItems()));
        }

        String processKey = cycle.getMetadata().getCycle().getCode();

        if (null == processKey || processKey.isEmpty()) {
            throw pmCycleException(ErrorCodes.PROCESS_NAME_IS_EMPTY, Map.of(CYCLE_UUID_PARAMETER_NAME, uuid), null);
        }

        try {
            var processId = deploymentService.deploy(cycle.getTemplate().getUuid());
            log.debug("Process definition id: {}", processId);
            intUpdateStatus(uuid, PMCycleStatus.REGISTERED, null); // todo move status map to BPMN or DMN

            var pmRuntimeProcess = PMRuntimeProcess.builder()
                    .bpmProcessId(processId)
                    .cycleUuid(uuid)
                    .businessKey(cycle.getEntryConfigKey())
                    .build();

            pmRuntimeProcess = pmProcessService.register(pmRuntimeProcess, PMProcessStatus.REGISTERED);
            log.debug("Registered PM process: {}", pmRuntimeProcess);

            return pmRuntimeProcess.getId();
        } catch (Exception e) {
            cycleFailed(processKey, uuid, e);
        }
        return null;
    }


    private void intStartCycle(UUID cycleUUID) {

        DictionaryFilter<PMCycleStatus> statusFilter = includeFilter(PMCycleStatus.REGISTERED);
        var cycle = cycleDAO.read(cycleUUID, statusFilter);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUUID,
                            CYCLE_STATUSES_PARAMETER_NAME, statusFilter.getItems()));
        }

        validateCycleForStart(cycle);

        DictionaryFilter<PMProcessStatus> processStatusFilter = includeFilter(PMProcessStatus.REGISTERED);
        var processes = pmProcessService.findByCycleUuidAndStatus(cycleUUID, processStatusFilter);
        if (isEmpty(processes) || processes.size() > 1) {
            throw new NotFoundException(PMProcessErrorCodes.PROCESS_NOT_FOUND_BY_CYCLE.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_NOT_FOUND_BY_CYCLE,
                            Map.of(CYCLE_UUID, cycleUUID, STATUS_FILTER, statusFilter)));
        }

        var process = processes.iterator().next();
        try {
            var processUUID = processManagerService.runProcessById(process.getBpmProcessId(), prepareFlowProperties(cycle));
            log.debug("Started process: {}", processUUID);
            pmProcessService.updateStatus(process.getId(), STARTED, processStatusFilter);
        } catch (ProcessExecutionException e) {
            cycleFailed(process.getBpmProcessId(), cycleUUID, e);
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
                new Condition(STATUS_CONDITION, EQUALS, STARTED.getId()),
                new Condition(TEMPLATE_UUID_CONDITION, EQUALS, cycle.getTemplate().getUuid())
        ));

        List<PMCycle> cycleList = cycleDAO.findAll(query, false);
        if (!isEmpty(cycleList)) {
            throw notFound(PM_CYCLE_NOT_ALLOWED_TO_START,
                    Map.of(CONDITION_PARAMETER_NAME, query.getFilters()));
        }
    }

    private List<PMForm> getFormsForCycleMetadata(PMCycleMetadata metadata) {
        return metadata.getCycle().getTimelinePoints().stream()
                .filter(tpe -> tpe.getType() == REVIEW)
                .map(review -> ((PMReviewElement) review).getForm())
                .map(el -> new PMForm(el.getId(), el.getKey(), el.getCode(), null))
                .map(this::fillFormJson)
                .collect(Collectors.toList());
    }

    private PMForm fillFormJson(PMForm form) {
        try {
            form.setJson(new String(resourceProvider.readFile(UUID.fromString(form.getId())).getFileContent(), UTF_8));
            return form;
        } catch (Exception e) {
            return null;
        }
    }

    private PMForm getPMForm(String formKey) {
        try {
            var formName = FilenameUtils.getName(getFormName(formKey));
            var formPath = FilenameUtils.getFullPathNoEndSeparator(formKey);
            var formFile = fileService.get(formPath, formName, false, null);
            return new PMForm(formFile.getUuid().toString(), formKey, formKey, null);
        } catch (Exception e) {
            throw new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "Form was not found", formKey, e);
        }
    }

    private PMForm getPMForm(UUID formUuid) {
        try {
            var formFile = fileService.get(formUuid, false);
            var formKey = FilenameUtils.concat(formFile.getPath(), formFile.getFileName());

            return new PMForm(formFile.getUuid().toString(), formKey, formKey, null);
        } catch (Exception e) {
            throw new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "Form was not found", formUuid.toString(), e);
        }
    }

    private PMForm getPMForm(PMFormRequest formRequest) {
        try {
            return getPMForm(UUID.fromString(formRequest.getId()));
        } catch (Exception e) {
            String formKey = formRequest.getKey() != null ? formRequest.getKey() : formRequest.getCode();
            return getPMForm(formKey);
        }
    }
}
