package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.dao.PMColleagueCycleDAO;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.REGISTERED;
import static com.tesco.pma.cycle.api.PMCycleStatus.STARTED;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_COLLEAGUE_CYCLE_ALREADY_EXISTS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_COLLEAGUE_CYCLE_NOT_EXIST;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class PMColleagueCycleServiceImpl implements PMColleagueCycleService {

    private final BatchService batchService;
    private final PMColleagueCycleDAO dao;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final PMProcessService pmProcessService;
    private final ProcessManagerService processManagerService;
    private final PMCycleDAO cycleDAO;

    private static final String CYCLE_UUID = "cycleUUID";
    private static final String STATUS_FILTER = "status_filter";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String CYCLE_STATUSES_PARAMETER_NAME = "statuses";
    public static final String COLLEAGUE_CYCLE_UUID = "colleagueCycleUuid";
    public static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";

    @Override
    public PMColleagueCycle get(UUID uuid) {
        var pmColleagueCycle = dao.read(uuid);
        if (pmColleagueCycle == null) {
            throw notFound(PM_COLLEAGUE_CYCLE_NOT_EXIST, Map.of(COLLEAGUE_CYCLE_UUID, uuid));
        }
        return pmColleagueCycle;
    }

    @Override
    public List<PMColleagueCycle> getByCycleUuid(UUID cycleUuid, UUID colleagueUuid, PMCycleStatus status) {
        return dao.getByParams(cycleUuid, colleagueUuid, status);
    }

    @Override
    public List<PMColleagueCycle> getByCycleUuidWithoutTimelinePoint(UUID cycleUuid, DictionaryFilter<PMCycleStatus> statusFilter) {
        return dao.getByCycleUuidWithoutTimelinePoint(cycleUuid, statusFilter);
    }

    @Override
    public void saveColleagueCycles(Collection<PMColleagueCycle> colleagueCycles) {
        batchService.executeDBOperationInBatch(colleagueCycles, dao::saveAll);
    }

    @Override
    public PMColleagueCycle create(PMColleagueCycle pmColleagueCycle) {
        try {
            pmColleagueCycle.setUuid(UUID.randomUUID());
            pmColleagueCycle.setCreationTime(Instant.now());
            dao.create(pmColleagueCycle);
        } catch (DuplicateKeyException ex) {
            throw new AlreadyExistsException(PM_COLLEAGUE_CYCLE_ALREADY_EXISTS.getCode(),
                    messageSourceAccessor.getMessage(PM_COLLEAGUE_CYCLE_ALREADY_EXISTS,
                            Map.of(CYCLE_UUID_PARAMETER_NAME, pmColleagueCycle.getCycleUuid(),
                                    COLLEAGUE_UUID_PARAMETER_NAME, pmColleagueCycle.getColleagueUuid())), ex);
        }
        return pmColleagueCycle;
    }

    @Override
    public PMColleagueCycle update(PMColleagueCycle pmColleagueCycle) {
        var updated = dao.update(pmColleagueCycle);
        if (1 != updated) {
            throw notFound(PM_COLLEAGUE_CYCLE_NOT_EXIST, Map.of(COLLEAGUE_CYCLE_UUID, pmColleagueCycle.getUuid()));
        }
        return pmColleagueCycle;
    }

    @Override
    public void delete(UUID uuid) {
        var deleted = dao.delete(uuid);
        if (1 != deleted) {
            throw notFound(PM_COLLEAGUE_CYCLE_NOT_EXIST, Map.of(COLLEAGUE_CYCLE_UUID, uuid));
        }
    }

    @Override
    public void changeStatusForColleague(UUID colleagueUuid, PMCycleStatus oldStatus, PMCycleStatus newStatus) {
        var changed = dao.changeStatusForColleague(colleagueUuid, oldStatus, newStatus);

        if (0 == changed) {
            throw notFound(PM_COLLEAGUE_CYCLE_NOT_EXIST, Map.of(COLLEAGUE_CYCLE_UUID, colleagueUuid));
        }
    }

    @Override
    public void start(UUID cycleUuid, UUID colleagueUuid) {

        DictionaryFilter<PMCycleStatus> statusFilter = includeFilter(REGISTERED, ACTIVE, STARTED);
        var cycle = cycleDAO.read(cycleUuid, statusFilter);
        if (null == cycle) {
            throw notFound(PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, cycleUuid,
                            CYCLE_STATUSES_PARAMETER_NAME, statusFilter.getItems()));
        }

        var processes = pmProcessService.findByCycleUuidAndStatus(cycleUuid, null);
        if (isEmpty(processes) || processes.size() > 1) {
            throw new NotFoundException(PMProcessErrorCodes.PROCESS_NOT_FOUND_BY_CYCLE.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_NOT_FOUND_BY_CYCLE,
                            Map.of(CYCLE_UUID, cycleUuid, STATUS_FILTER, "null")));
        }

        var process = processes.iterator().next();
        try {
            var processUUID = processManagerService.runProcessById(process.getBpmProcessId(),
                    prepareFlowProperties(cycle, colleagueUuid));
            log.debug("Started process: {}", processUUID);
        } catch (ProcessExecutionException e) {
            log.error("Can't start process: {}", process.getBpmProcessId());
        }
    }

    private NotFoundException notFound(ErrorCodeAware codeAware, Map<String, ?> params) {
        throw new NotFoundException(codeAware.getCode(), messageSourceAccessor.getMessage(codeAware.getCode(), params));
    }

    private Map<String, Object> prepareFlowProperties(PMCycle cycle, UUID colleagueUuid) {
        Map<String, Object> props = new HashMap<>();
        props.put(TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());
        props.put(FlowParameters.PM_CYCLE.name(), cycle);
        props.put(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid);
        return props;
    }

}
