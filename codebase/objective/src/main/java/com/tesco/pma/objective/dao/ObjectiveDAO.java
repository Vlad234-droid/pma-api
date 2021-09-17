package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;
import org.apache.ibatis.annotations.Param;

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

}
