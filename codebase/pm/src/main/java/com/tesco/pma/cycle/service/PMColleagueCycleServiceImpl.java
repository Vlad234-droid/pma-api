package com.tesco.pma.cycle.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.dao.PMColleagueCycleDAO;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.exception.ErrorCodes.PM_COLLEAGUE_CYCLE_ALREADY_EXISTS;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_COLLEAGUE_CYCLE_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class PMColleagueCycleServiceImpl implements PMColleagueCycleService {

    public static final String COLLEAGUE_CYCLE_UUID = "colleagueCycleUuid";
    private final BatchService batchService;
    private final PMColleagueCycleDAO dao;
    private final NamedMessageSourceAccessor messageSourceAccessor;

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
    public List<PMColleagueCycle> getActiveByCycleUuidWithoutTimelinePoint(UUID cycleUuid, Instant startTime) {
        return dao.getByCycleUuidWithoutTimelinePoint(cycleUuid, PMCycleStatus.ACTIVE, startTime);
    }

    @Override
    public void saveColleagueCycles(Collection<PMColleagueCycle> colleagueCycles) {
        batchService.executeDBOperationInBatch(colleagueCycles, dao::saveAll);
    }

    @Override
    public PMColleagueCycle create(PMColleagueCycle pmColleagueCycle) {
        try {
            pmColleagueCycle.setUuid(UUID.randomUUID());
            dao.create(pmColleagueCycle);
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(PM_COLLEAGUE_CYCLE_ALREADY_EXISTS.getCode(),
                    messageSourceAccessor.getMessage(PM_COLLEAGUE_CYCLE_ALREADY_EXISTS,
                            Map.of(COLLEAGUE_CYCLE_UUID, pmColleagueCycle.getUuid())), null, ex);
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

    private NotFoundException notFound(ErrorCodeAware codeAware, Map<String, ?> params) {
        throw new NotFoundException(codeAware.getCode(), messageSourceAccessor.getMessage(codeAware.getCode(), params));
    }
}
