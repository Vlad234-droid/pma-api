package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
import com.tesco.pma.cycle.api.ReviewCounter;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.service.PMProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.cycle.api.PMCycleStatus.*;
import static com.tesco.pma.cycle.exception.ErrorCodes.*;
import static java.lang.Boolean.TRUE;
import static java.util.Set.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class PMCycleServiceImpl implements PMCycleService {

    private final PMCycleDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessManagerService processManagerService;
    private final PMProcessService pmProcessService;

    public static final String NOT_IMPLEMENTED_YET = "Not implemented yet";
    private static final String ORG_KEY_PARAMETER_NAME = "organisationKey";
    private static final String TEMPLATE_UUID_PARAMETER_NAME = "templateUUID";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String INCLUDE_METADATA_PARAMETER_NAME = "includeMetadata";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";

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
    public PMCycle create(@NotNull PMCycle cycle) {
        log.debug("Request to create Performance cycle : {}", cycle);
        cycle.setUuid(UUID.randomUUID());
        try {
            cycleDAO.create(cycle);
            log.debug("Performance cycle created UUID: {}", cycle.getUuid());
            return cycle;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    PM_CYCLE_ALREADY_EXISTS,
                    Map.of(ORG_KEY_PARAMETER_NAME, cycle.getEntryConfigKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, cycle.getTemplateUUID()), e);
        }
    }

    @Override
    @Transactional
    public PMCycle publish(@NotNull PMCycle cycle) {
        log.debug("Request to publish Performance cycle : {}", cycle);
        create(cycle);
        log.debug("Starting process");
        String processUUID = null;
        try {
            Map<String, Object> props = Map.of("needsObjective", TRUE,
                    "needsEyr", TRUE,
                    "needsMyr", TRUE);
            processUUID = processManagerService.runProcess("type_1",  props);
            log.debug("Started process: {}", processUUID);
        } catch (ProcessExecutionException e) {
            //TODO throw ex or return cycle
            e.printStackTrace();
            updateStatus(cycle.getUuid(), FAILED);
        }

        if (null == processUUID) {
            throw new NullPointerException();
            //TODO throw ex. Check UUID
        }
        PMRuntimeProcess pmRuntimeProcess = PMRuntimeProcess.builder()
                .bpmProcessId(UUID.fromString(processUUID))
                .cycleUuid(cycle.getUuid())
                .businessKey(cycle.getEntryConfigKey())
                .build();
        pmRuntimeProcess = pmProcessService.register(pmRuntimeProcess);
        log.debug("Registered PM process: {}", pmRuntimeProcess);

        pmProcessService.updateStatus(pmRuntimeProcess.getId(), PMProcessStatus.STARTED,
                DictionaryFilter.includeFilter(PMProcessStatus.REGISTERED));

        return cycle;
    }

    @Override
    @Transactional
    public PMCycle updateStatus(UUID uuid, PMCycleStatus status) {

        var cycle = cycleDAO.read(uuid);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        cycle.setStatus(status);

        DictionaryFilter<PMCycleStatus> statusFilter = UPDATE_STATUS_RULE_MAP.get(status);

        if (1 == cycleDAO.updateStatus(uuid, status, statusFilter)) {
            return cycle;
        } else {
            throw notFound(PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            PREV_STATUSES_PARAMETER_NAME, statusFilter));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PMCycle get(UUID uuid) {
        var res = cycleDAO.read(uuid);
        if (res == null) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }
        return res;
    }

    @Override
    public PMCycle update(PMCycle uuid, Collection<PMCycleStatus> oldStatuses) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PMCycle> getByStatus(PMCycleStatus status) {
        var results = cycleDAO.getByStatus(status);
        if (results == null) {
            throw notFound(PM_CYCLE_NOT_FOUND,
                    Map.of(STATUS_PARAMETER_NAME, status));
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public PMCycle getCurrentByColleague(UUID colleagueUuid) {
        DictionaryFilter<PMCycleStatus> activeFilter = DictionaryFilter.includeFilter(Set.of(ACTIVE));
        List<PMCycle> result = cycleDAO.getByColleague(colleagueUuid, activeFilter);
        if (result == null || result.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return result.iterator().next();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PMCycle> getByColleague(UUID colleagueUuid) {
        var results = cycleDAO.getByColleague(colleagueUuid);
        if (results == null || results.isEmpty()) {
            throw notFound(PM_CYCLE_NOT_FOUND_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PMCycleTimelinePoint> getCycleTimeline(UUID uuid) {
        var timeline = cycleDAO.readTimeline(uuid);
        if (timeline == null) {
            throw new NotFoundException(PM_CYCLE_METADATA_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PM_CYCLE_METADATA_NOT_FOUND,
                            Map.of(CYCLE_UUID_PARAMETER_NAME, uuid)));
        }
        return timeline;
    }

    @Override
    public void updateJsonMetadata(UUID uuid, String metadata) {
        cycleDAO.updateMetadata(uuid, metadata);
    }

    @Override
    public List<PMCycleTimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid) {
        PMCycle currentCycleByColleague = getCurrentByColleague(colleagueUuid);
        List<PMCycleTimelinePoint> cycleTimeline = getCycleTimeline(currentCycleByColleague.getUuid());

        return cycleTimeline.stream()
                .peek(ctp -> ctp.setStatus(cycleDAO.getReviewsCountByStatus(ctp.getType(),
                        currentCycleByColleague.getUuid(),
                        colleagueUuid).stream().findFirst().orElse(new ReviewCounter()).getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PMCycle> getAll(boolean includeMetadata) {
        var results = cycleDAO.getAll(includeMetadata);
        if (results == null) {
            throw notFound(PM_CYCLE_NOT_FOUND,
                    Map.of(INCLUDE_METADATA_PARAMETER_NAME, includeMetadata));
        }
        return results;
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


}
