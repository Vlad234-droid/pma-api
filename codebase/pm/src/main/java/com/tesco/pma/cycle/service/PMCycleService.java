package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
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

    /**
     * Returns the current active performance cycle
     *
     * @param colleagueUuid Colleague identifier
     * @return performance cycle
     *
     * @throws NotFoundException if PMCycle doesn't found
     */
    PMCycle getCurrentByColleague(@NotNull UUID colleagueUuid);

    /**
     * Get list of PMCycle's for an employee
     *
     * @param colleagueUuid Colleague identifier
     * @return found list of PMCycle's
     *
     * @throws NotFoundException if PMCycle doesn't found
     */
    List<PMCycle> getByColleague(@NotNull UUID colleagueUuid);

    /**
     * Returns cycle timeline by uuid
     *
     * @param uuid process identifier
     * @return the timeline of the process
     * @throws com.tesco.pma.exception.NotFoundException if the was not found
     */
    List<PMCycleTimelinePoint> getCycleTimeline(@NotNull UUID uuid);

    /**
     * todo remove after UAT
     * Stores cycle metadata for the cycle
     *
     * @param uuid process UUID
     * @param metadata    process metadata
     */
    void updateJsonMetadata(@NotNull UUID uuid, @NotNull String metadata);

    List<PMCycleTimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid);

    List<PMCycle> getAll(boolean includeMetadata);
}

