package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.CompositePMCycleMetadataResponse;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;

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
                   UUID loggedUserUUID);

    /**
     * Publish performance cycle
     *
     * @param cycle PMCycle
     * @return published PMCycle
     */
    PMCycle publish(@NotNull PMCycle cycle,
                    UUID loggedUserUUID);

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
     * Update PMCycle status with status filter
     *
     * @param uuid         PMCycle uuid
     * @param status       new status
     * @param statusFilter allowed statuses or null
     * @return updated PMCycle
     * @throws NotFoundException if PMCycle doesn't exist
     */
    PMCycle updateStatus(@NotNull UUID uuid, @NotNull PMCycleStatus status, DictionaryFilter<PMCycleStatus> statusFilter);

    /**
     * Get PMCycle by uuid
     *
     * @param uuid PMCycle uuid
     * @return found PMCycle
     * @throws NotFoundException if PMCycle doesn't exist
     */
    CompositePMCycleResponse get(@NotNull UUID uuid, boolean includeForms);

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
     * Returns the current active performance cycle metadata
     *
     * @param colleagueUuid Colleague identifier
     * @return performance cycle
     * @throws NotFoundException if PMCycle doesn't found
     */
    CompositePMCycleMetadataResponse getCurrentMetadataByColleague(@NotNull UUID colleagueUuid, boolean includeForms);

    /**
     * Get list of PMCycle's for an employee
     *
     * @param colleagueUuid Colleague identifier
     * @return found list of PMCycle's
     * @throws NotFoundException if PMCycle doesn't found
     */
    List<PMCycle> getByColleague(@NotNull UUID colleagueUuid);

    List<PMCycle> getAll(RequestQuery requestQuery, boolean includeMetadata);

    /**
     * Get PMCycleMetadata by file UUID
     *
     * @param fileUuid File UUID
     * @return CompositePMCycleMetadata
     */
    CompositePMCycleMetadataResponse getFileMetadata(@NotNull UUID fileUuid, boolean includeForms);

    /**
     * Deploy pm cycle
     *
     * @param uuid performance cycle UUID
     * @return deployed runtime process UUID
     */
    UUID deploy(UUID uuid);

    /**
     * Start performance cycle by UUID
     *
     * @param uuid performance cycle UUID
     */
    void start(UUID uuid);

    /**
     * Start performance cycle
     *
     * @param cycle performance cycle
     */
    void start(PMCycle cycle);

    void completeCycle(UUID cycleUUID);
}

