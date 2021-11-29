package com.tesco.pma.review.dao;

import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/**
 * Interface to perform database operation on objectives
 */
public interface ObjectiveSharingDAO {

    /**
     * Creates record into shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return number of inserted rows
     */
    int shareObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

    /**
     * Deletes record from shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return number of deleted rows
     */
    int stopSharingObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

    /**
     * Check if record exists into shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return true/false
     */
    boolean isColleagueShareObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

}
