package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Performance cycle service
 */
public interface PMCycleService {

    /**
     * Creates performance cycle
     *
     * @param cycle PMCycle
     * @return created PMCycle
     *
     * @throws DatabaseConstraintViolationException PMCycle already exist.
     */
    PMCycle create(@NotNull PMCycle cycle);

    /**
     * Publish performance cycle
     *
     * @param cycle PMCycle
     * @return published PMCycle
     */
    PMCycle publish(@NotNull PMCycle cycle);

    /**
     * Update PMCycle status
     *
     * @param uuid PMCycle uuid
     * @param status new status
     * @return updated PMCycle
     *
     * @throws NotFoundException if PMCycle doesn't exist
     */
    PMCycle updateStatus(@NotNull UUID uuid,
                         @NotNull PMCycleStatus status);

    /**
     * Get PMCycle by uuid
     *
     * @param uuid PMCycle uuid
     * @return found PMCycle
     *
     * @throws NotFoundException if PMCycle doesn't exist
     */
    PMCycle get(@NotNull UUID uuid);

    PMCycle update(@NotNull PMCycle uuid,
                   @NotNull Collection<PMCycleStatus> oldStatuses);

    /**
     * Get list of PMCycle's by status
     *
     * @param status PMCycle status
     * @return found list of PMCycle's
     *
     * @throws NotFoundException if PMCycle doesn't found
     */
    List<PMCycle> getByStatus(@NotNull PMCycleStatus status);
}

