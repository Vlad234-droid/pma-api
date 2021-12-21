package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PMColleagueCycleService {

    /**
     * Gets pm colleague cycle by its uuid
     *
     * @param uuid - identifier
     * @return PM colleague cycle or null if nothing found
     */
    PMColleagueCycle get(UUID uuid);

    /**
     * Gets list of pm colleague cycles
     *
     * @param cycleUuid     - PM cycle identifier
     * @param colleagueUuid - colleague identifier
     * @param status        - PM colleague cycle status
     * @return - collection of PM colleague cycles
     */
    List<PMColleagueCycle> getByCycleUuid(UUID cycleUuid, UUID colleagueUuid, PMCycleStatus status);

    /**
     * Gets list of pm colleague cycles without timeline points
     *
     * @param cycleUuid - PM cycle identifier
     * @return - collection of PM colleague cycles
     */
    List<PMColleagueCycle> getActiveByCycleUuidWithoutTimelinePoint(UUID cycleUuid);

    /**
     * Stores batched collection of PM colleague cycles
     *
     * @param colleagueCycles - objects to be stored
     */
    void saveColleagueCycles(Collection<PMColleagueCycle> colleagueCycles);

    /**
     * Stores PM colleague cycle
     *
     * @param pmColleagueCycle - object to be stored
     * @return stored object
     */
    PMColleagueCycle create(PMColleagueCycle pmColleagueCycle);

    /**
     * Updates PM colleague cycle
     *
     * @param pmColleagueCycle - object to be updated
     * @return updated object
     */
    PMColleagueCycle update(PMColleagueCycle pmColleagueCycle);

    /**
     * Deletes PM colleague cycle by its uuid
     *
     * @param uuid - identifier
     */
    void delete(UUID uuid);

    /**
     * Changes status for colleague cycle handler
     *
     * @param colleagueUuid - colleague identifier
     * @param oldStatus     - previous status
     * @param newStatus     - new status
     */
    void changeStatusForColleague(UUID colleagueUuid, PMCycleStatus oldStatus, PMCycleStatus newStatus);
}
