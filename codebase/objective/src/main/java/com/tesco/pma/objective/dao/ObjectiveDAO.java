package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;
import com.tesco.pma.objective.domain.WorkingGroupObjective;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on objective
 */
public interface ObjectiveDAO {

    /**
     * Returns a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return a GroupObjective
     */
    GroupObjective getGroupObjective(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Returns a list of group objectives
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a list of Group Objectives
     */
    List<GroupObjective> getGroupObjectivesByBusinessUnitUuid(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Returns a list of working group objectives
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a list of Group Objectives
     */
    List<GroupObjective> getWorkingGroupObjectivesByBusinessUnitUuid(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Creates a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of created group objectives
     */
    int createGroupObjective(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Delete a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return number of deleted group objectives
     */
    int deleteGroupObjective(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Returns max version of group objectives
     *
     * @param businessUnitUuid an identifier of business unit
     * @return max version of group objectives
     */
    int getMaxVersionGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Returns a personal objective
     *
     * @param personalObjectiveUuid an identifier
     * @return a PersonalObjective
     */
    PersonalObjective getPersonalObjective(@Param("personalObjectiveUuid") UUID personalObjectiveUuid);

    /**
     * Returns a personal objective by performance cycle, colleague and sequence number.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param sequenceNumber       a sequence number of personal objective
     * @return a PersonalObjective
     */
    PersonalObjective getPersonalObjectiveForColleague(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                                                       @Param("colleagueUuid") UUID colleagueUuid,
                                                       @Param("sequenceNumber") Integer sequenceNumber);

    /**
     * Returns a personal objective by performance cycle, colleague and sequence number.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @return a PersonalObjective
     */
    List<PersonalObjective> getPersonalObjectivesForColleague(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                                                              @Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Creates a personal objective
     *
     * @param personalObjective a PersonalObjective
     * @return number of created personal objectives
     */
    int createPersonalObjective(@Param("personalObjective") PersonalObjective personalObjective);

    /**
     * Update a personal objective
     *
     * @param personalObjective a PersonalObjective
     * @return number of updated personal objectives
     */
    int updatePersonalObjective(@Param("personalObjective") PersonalObjective personalObjective);

    /**
     * Delete a personal objective
     *
     * @param personalObjectiveUuid an identifier
     * @return number of deleted personal objectives
     */
    int deletePersonalObjective(@Param("personalObjectiveUuid") UUID personalObjectiveUuid);

    /**
     * Insert or update a working group objective
     *
     * @param workingGroupObjective a WorkingGroupObjective
     * @return number of inserted or updated working group objectives
     */
    int insertOrUpdateWorkingGroupObjective(@Param("workingGroupObjective") WorkingGroupObjective workingGroupObjective);

    /**
     * Returns a working group objective
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a WorkingGroupObjective
     */
    WorkingGroupObjective getWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Delete a working group objective
     *
     * @param businessUnitUuid an identifier of business unit
     * @return number of deleted working group objectives
     */
    int deleteWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

}
