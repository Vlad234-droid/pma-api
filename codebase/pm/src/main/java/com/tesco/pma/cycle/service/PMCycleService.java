package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;

import javax.validation.constraints.NotNull;
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
     * @throws DatabaseConstraintViolationException PMCycle already exist.
     */
    PMCycle create(@NotNull PMCycle cycle,
                   UUID loggedUserName);

    /**
     * Publish performance cycle
     *
     * @param cycle PMCycle
     * @return published PMCycle
     */
    PMCycle publish(@NotNull PMCycle cycle,
                    UUID loggedUserName);

    /**
     * Update PMCycle status
     *
     * @param uuid   PMCycle uuid
     * @param status new status
     * @return updated PMCycle
     * @throws NotFoundException if PMCycle doesn't exist
     */
    PMCycle updateStatus(@NotNull UUID uuid,
                         @NotNull PMCycleStatus status);

    /**
     * Get PMCycle by uuid
     *
     * @param uuid PMCycle uuid
     * @return found PMCycle
     * @throws NotFoundException if PMCycle doesn't exist
     */
    PMCycle get(@NotNull UUID uuid);

    PMCycle update(@NotNull PMCycle cycle);

    /**
     * Returns the current active performance cycle
     *
     * @param colleagueUuid Colleague identifier
     * @return performance cycle
     * @throws NotFoundException if PMCycle doesn't found
     */
    PMCycle getCurrentByColleague(@NotNull UUID colleagueUuid);

    /**
     * Get list of PMCycle's for an employee
     *
     * @param colleagueUuid Colleague identifier
     * @return found list of PMCycle's
     * @throws NotFoundException if PMCycle doesn't found
     */
    List<PMCycle> getByColleague(@NotNull UUID colleagueUuid);

    /**
     * todo remove after UAT
     * Stores cycle metadata for the cycle
     *
     * @param uuid     process UUID
     * @param metadata process metadata
     */
    void updateJsonMetadata(@NotNull UUID uuid, @NotNull String metadata);

    List<PMCycle> getAll(boolean includeMetadata);

    /**
     * Get PMCycleMetadata by file UUID
     *
     * @param fileUuid File UUID
     * @return PMCycleMetadata
     */
    PMCycleMetadata getMetadata(@NotNull UUID fileUuid);

    String  deploy(PMCycle cycle);

    void start(UUID cycleUUID, String processId);
}

