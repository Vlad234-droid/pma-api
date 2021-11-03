package com.tesco.pma.cycle.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleConfigurationStatus;
import com.tesco.pma.cycle.api.PMCycleConfiguration;
import com.tesco.pma.cycle.dao.PmCycleConfigurationDAO;
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

import static com.tesco.pma.cycle.api.PMCycleConfigurationStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleConfigurationStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMCycleConfigurationStatus.INACTIVE;
import static com.tesco.pma.cycle.api.PMCycleConfigurationStatus.REMOVED;
import static com.tesco.pma.cycle.exception.ErrorCodes.CYCLE_CONFIGURATIONS_NOT_FOUND;
import static com.tesco.pma.cycle.exception.ErrorCodes.CYCLE_CONFIGURATION_ALREADY_EXISTS;
import static com.tesco.pma.cycle.exception.ErrorCodes.CYCLE_CONFIGURATION_NOT_FOUND_BY_UUID;
import static com.tesco.pma.cycle.exception.ErrorCodes.CYCLE_CONFIGURATION_NOT_FOUND_FOR_STATUS_UPDATE;

@Service
@RequiredArgsConstructor
public class PMCycleConfigurationServiceImpl implements PMCycleConfigurationService {

    private final PmCycleConfigurationDAO cycleDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public static final String NOT_IMPLEMENTED_YET = "Not implemented yet";
    private static final String ORG_KEY_PARAMETER_NAME = "organisationKey";
    private static final String TEMPLATE_UUID_PARAMETER_NAME = "templateUUID";
    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";

    private static final Map<PMCycleConfigurationStatus, Collection<PMCycleConfigurationStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                ACTIVE, List.of(INACTIVE, DRAFT),
                INACTIVE, List.of(ACTIVE, DRAFT),
                DRAFT, List.of(ACTIVE, INACTIVE),
                REMOVED, List.of(ACTIVE, INACTIVE, DRAFT)
        );
    }

    @Override
    public PMCycleConfiguration create(@NotNull PMCycleConfiguration config) {
        config.setUuid(UUID.randomUUID());
        try {
            cycleDAO.createCycle(config);
            return config;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    CYCLE_CONFIGURATION_ALREADY_EXISTS,
                    Map.of(ORG_KEY_PARAMETER_NAME, config.getEntryConfigKey(),
                            TEMPLATE_UUID_PARAMETER_NAME, config.getTemplateUUID()), e);
        }
    }

    @Override
    public PMCycleConfiguration publish(@NotNull PMCycleConfiguration config) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public PMCycleConfiguration updateStatus(UUID uuid, PMCycleConfigurationStatus status) {

        var cycle = cycleDAO.getPmCycle(uuid);
        if (null == cycle) {
            throw notFound(CYCLE_CONFIGURATION_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }

        cycle.setStatus(status);
        var prevStatuses = UPDATE_STATUS_RULE_MAP.get(status);

        if (1 == cycleDAO.updateCycleStatus(uuid, status, prevStatuses)) {
            return cycle;
        } else {
            throw notFound(CYCLE_CONFIGURATION_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            PREV_STATUSES_PARAMETER_NAME, prevStatuses));
        }
    }

    @Override
    public PMCycleConfiguration getPMCycleConfigByUUID(UUID uuid) {
        var res = cycleDAO.getPmCycle(uuid);
        if (res == null) {
            throw notFound(CYCLE_CONFIGURATION_NOT_FOUND_BY_UUID,
                    Map.of(CYCLE_UUID_PARAMETER_NAME, uuid));
        }
        return res;
    }

    @Override
    public PMCycleConfiguration updatePerformanceCycle(PMCycleConfiguration uuid, Collection<PMCycleConfigurationStatus> oldStatuses) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public List<PMCycleConfiguration> getAllPMCycleConfigForStatus(PMCycleConfigurationStatus status) {
        var results = cycleDAO.getAllPmCyclesForStatus(status);
        if (results == null) {
            throw notFound(CYCLE_CONFIGURATIONS_NOT_FOUND,
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
