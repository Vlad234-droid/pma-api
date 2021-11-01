package com.tesco.pma.cycle.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PerformanceCycle;
import com.tesco.pma.cycle.dao.PmCycleDAO;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.*;
import static com.tesco.pma.cycle.exception.ErrorCodes.*;

@Service
@RequiredArgsConstructor
public class PerformanceCycleServiceImpl implements PerformanceCycleService {

    private final PmCycleDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public static final String NOT_IMPLEMENTED_YET = "Not implemented yet";
    private static final String ORG_KEY_PARAMETER_NAME = "organisationKey";
    private static final String TEMPLATE_UUID_PARAMETER_NAME = "templateUUID";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";

    private static final Map<PMCycleStatus, Collection<PMCycleStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                ACTIVE, List.of(INACTIVE, DRAFT),
                INACTIVE, List.of(ACTIVE, DRAFT),
                DRAFT, List.of(ACTIVE, INACTIVE),
                REMOVED, List.of(ACTIVE, INACTIVE, DRAFT)
        );
    }

    @Override
    public PerformanceCycle create(@NotNull PerformanceCycle cycle) {
        cycle.setUuid(UUID.randomUUID());
        try {
            cycleDAO.createCycle(cycle);
            return cycle;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    CYCLE_ALREADY_EXISTS,
                    Map.of(ORG_KEY_PARAMETER_NAME, cycle.getOrganisationKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, cycle.getTemplateUUID()), e);
        }
    }

    @Override
    public PerformanceCycle publish(@NotNull PerformanceCycle cycle) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public PerformanceCycle updateStatus(UUID uuid, PMCycleStatus status) {

        var cycle = cycleDAO.getPmCycle(uuid);
        if (null == cycle) {
            throw notFound(CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        cycle.setStatus(status);
        var prevStatuses = UPDATE_STATUS_RULE_MAP.get(status);

        if (1 == cycleDAO.updateCycleStatus(uuid, status, prevStatuses)) {
            return cycle;
        } else {
            throw notFound(CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            PREV_STATUSES_PARAMETER_NAME, prevStatuses));
        }
    }

    @Override
    public PerformanceCycle getPerformanceCycle(UUID uuid) {
        var res = cycleDAO.getPmCycle(uuid);
        if (res == null) {
            throw notFound(CYCLE_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }
        return res;
    }

    @Override
    public PerformanceCycle updatePerformanceCycle(PerformanceCycle uuid, Collection<PMCycleStatus> oldStatuses) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public List<PerformanceCycle> getAllPerformanceCyclesForStatus(PMCycleStatus status) {
        List<PerformanceCycle> results = cycleDAO.getAllPmCyclesForStatus(status);
        if (results == null) {
            throw notFound(CYCLES_NOT_FOUND,
                    Map.of(STATUS_PARAMETER_NAME, status));
        }
        return results;
    }

    //TODO refactor to common solution (include @com.tesco.pma.review.service.ReviewServiceImpl)
    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return notFound(errorCode, params, null);
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params, Throwable cause) {
        return new NotFoundException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }

    private DatabaseConstraintViolationException databaseConstraintViolation(ErrorCodeAware errorCode,
                                                                             Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }


}
