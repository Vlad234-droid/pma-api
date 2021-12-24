package com.tesco.pma.pdp.dao;

import com.tesco.pma.pdp.domain.PDPGoal;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on Personal Development Plan
 */
public interface PDPDao { // NOPMD

    /**
     * Create a PDP Goal
     *
     * @param colleagueUuid is colleague identifier
     * @param goal          is PDP goal
     * @return number of created PDP goals
     */
    int createGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("goal") PDPGoal goal);

    /**
     * Update a PDP Goal
     *
     * @param colleagueUuid is colleague identifier
     * @param goal          is PDP goal
     * @return number of updated rows
     */
    int updateGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("goal") PDPGoal goal);

    /**
     * Delete PDP Goal from Plan by its colleague and number
     *
     * @param colleagueUuid is colleague identifier
     * @param number        is goal's number
     * @return number of deleted rows
     *
     */
    int deleteGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("number") Integer number);

    /**
     * Delete PDP Goal from Plan by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuid is goal identifier
     * @return number of deleted rows
     */
    int deleteGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("goalUuid") UUID goalUuid);

    /**
     * Get a PDP Goal by its colleague and number
     *
     * @param colleagueUuid is colleague identifier
     * @param number        is goal's number
     * @return PDP Goal by its colleagueUuid and number
     */
    PDPGoal readGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("number") Integer number);

    /**
     * Get a PDP Goal by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuid is goal identifier
     * @return PDP Goal by its uuid
     */
    PDPGoal readGoal(@Param("colleagueUuid") UUID colleagueUuid, @Param("goalUuid") UUID goalUuid);

    /**
     * Get a list of PDP Goals by its colleague ordered by its number
     *
     * @param colleagueUuid is colleague identifier
     * @return list of PDP Goals by its colleagueUuid
     */
    List<PDPGoal> readGoals(@Param("colleagueUuid") UUID colleagueUuid);
}