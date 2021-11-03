package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMCycleConfiguration;
import com.tesco.pma.cycle.api.PMCycleConfigurationStatus;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Performance cycle configuration service
 */
public interface PMCycleConfigurationService {

    /**
     * Creates performance cycle configuration
     *
     * @param config PMCycleConfiguration
     * @return created PMCycleConfiguration
     *
     * @throws DatabaseConstraintViolationException PMCycle already exist.
     */
    PMCycleConfiguration create(@NotNull PMCycleConfiguration config);

    /**
     * Publish performance cycle configuration
     *
     * @param config PMCycleConfiguration
     * @return published PMCycleConfiguration
     */
    PMCycleConfiguration publish(@NotNull PMCycleConfiguration config);

    /**
     * Update PMCycleConfiguration status
     *
     * @param uuid PMCycleConfiguration uuid
     * @param status new status
     * @return updated PMCycleConfiguration
     *
     * @throws NotFoundException if PMCycleConfiguration doesn't exist
     */
    PMCycleConfiguration updateStatus(@NotNull UUID uuid,
                                      @NotNull PMCycleConfigurationStatus status);

    /**
     * Get PMCycleConfiguration by uuid
     *
     * @param uuid PMCycleConfiguration uuid
     * @return found PMCycleConfiguration
     *
     * @throws NotFoundException if PMCycleConfiguration doesn't exist
     */
    PMCycleConfiguration getPMCycleConfigByUUID(@NotNull UUID uuid);

    PMCycleConfiguration updatePerformanceCycle(@NotNull PMCycleConfiguration uuid,
                                                @NotNull Collection<PMCycleConfigurationStatus> oldStatuses);

    /**
     * Get list of PMCycleConfiguration's by status
     *
     * @param status PMCycleConfiguration status
     * @return found list of PMCycleConfiguration's
     *
     * @throws NotFoundException if PMCycleConfiguration doesn't found
     */
    List<PMCycleConfiguration> getAllPMCycleConfigForStatus(@NotNull PMCycleConfigurationStatus status);
}

