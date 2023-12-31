package com.tesco.pma.pdp.dao;

import com.tesco.pma.pdp.api.PDPGoal;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on Personal Development Plan
 */
public interface PDPDao {

    /**
     * Create a PDP Goal
     *
     * @param goal          is PDP goal
     * @return number of created PDP goals
     */
    int createGoal(@Param("goal") PDPGoal goal);

    /**
     * Update a PDP Goal
     *
     * @param goal          is PDP goal
     * @return number of updated rows
     */
    int updateGoal(@Param("goal") PDPGoal goal);

    /**
     * Delete PDP Goal from Plan by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuid is goal identifier
     * @return number of deleted rows
     */
    int deleteGoalByUuidAndColleague(@Param("colleagueUuid") UUID colleagueUuid, @Param("goalUuid") UUID goalUuid);

    /**
     * Get a PDP Goal by its colleague and number
     *
     * @param colleagueUuid is colleague identifier
     * @param number        is goal's number
     * @return PDP Goal by its colleagueUuid and number
     */
    PDPGoal readGoalByColleagueAndNumber(@Param("colleagueUuid") UUID colleagueUuid, @Param("number") Integer number);

    /**
     * Get a PDP Goal by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuid is goal identifier
     * @return PDP Goal by its uuid
     */
    PDPGoal readGoalByUuid(@Param("colleagueUuid") UUID colleagueUuid, @Param("goalUuid") UUID goalUuid);

    /**
     * Get a list of PDP Goals by its colleague ordered by its number
     *
     * @param colleagueUuid is colleague identifier
     * @return list of PDP Goals by its colleagueUuid
     */
    List<PDPGoal> readGoalsByColleague(@Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Get the earliest achievement date of colleague's goals
     * @param colleagueUuid is colleague identifier
     * @return early achievement date of goals
     */
    LocalDate readEarlyAchievementDate(@Param("colleagueUuid") UUID colleagueUuid);
}