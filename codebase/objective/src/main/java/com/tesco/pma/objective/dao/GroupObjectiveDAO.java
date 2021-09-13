package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.GroupObjective;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/**
 * Interface to perform database operation on group objective
 */
public interface GroupObjectiveDAO {

    /**
     * Returns a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return a GroupObjective
     */
    GroupObjective get(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Inserts a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of inserted group objectives
     */
    int insert(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Update a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of updated group objectives
     */
    int update(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Delete a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return number of deleted group objectives
     */
    int delete(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

}
