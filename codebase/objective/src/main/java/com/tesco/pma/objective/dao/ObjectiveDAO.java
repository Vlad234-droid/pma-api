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
     * Creates a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of created group objectives
     */
    int createGroupObjective(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Update a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of updated group objectives
     */
    int updateGroupObjective(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Delete a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return number of deleted group objectives
     */
    int deleteGroupObjective(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Returns a personal objective
     *
     * @param personalObjectiveUuid an identifier
     * @return a PersonalObjective
     */
    PersonalObjective getPersonalObjective(@Param("personalObjectiveUuid") UUID personalObjectiveUuid);

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
     * Creates a working group objective
     *
     * @param workingGroupObjective a WorkingGroupObjective
     * @return number of created working group objectives
     */
    int createWorkingGroupObjective(@Param("workingGroupObjective") WorkingGroupObjective workingGroupObjective);

    /**
     * Update a working group objective
     *
     * @param workingGroupObjective a WorkingGroupObjective
     * @return number of updated working group objectives
     */
    int updateWorkingGroupObjective(@Param("workingGroupObjective") WorkingGroupObjective workingGroupObjective);

    /**
     * Returns a working group objective
     *
     * @param businessUnitUuid     an identifier of business unit
     * @param performanceCycleUuid an identifier of performance cycle
     * @param sequenceNumber       a sequence number of group objective
     * @return a WorkingGroupObjective
     */
    WorkingGroupObjective getWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid,
                                                   @Param("performanceCycleUuid") UUID performanceCycleUuid,
                                                   @Param("sequenceNumber") Integer sequenceNumber);

    /**
     * Returns a all group objectives by business unit and performance cycle
     *
     * @param businessUnitUuid     an identifier of business unit
     * @param performanceCycleUuid an identifier of performance cycle
     * @return a list of Group Objectives
     */
    List<GroupObjective> getAllGroupObjectives(@Param("businessUnitUuid") UUID businessUnitUuid,
                                               @Param("performanceCycleUuid") UUID performanceCycleUuid);

    /**
     * Delete a working group objective
     *
     * @param businessUnitUuid     an identifier of business unit
     * @param performanceCycleUuid an identifier of performance cycle
     * @param sequenceNumber       a sequence number of group objective
     * @return number of deleted working group objectives
     */
    int deleteWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid,
                                    @Param("performanceCycleUuid") UUID performanceCycleUuid,
                                    @Param("sequenceNumber") Integer sequenceNumber);

}
