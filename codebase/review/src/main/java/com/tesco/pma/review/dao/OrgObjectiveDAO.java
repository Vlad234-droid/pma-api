package com.tesco.pma.review.dao;

import com.tesco.pma.review.domain.OrgObjective;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on organisation objectives
 */
public interface OrgObjectiveDAO {

    /**
     * Returns an organisation objective
     *
     * @param uuid an identifier
     * @return an OrgObjective
     */
    OrgObjective read(@Param("uuid") UUID uuid);

    /**
     * Returns a list of organisation objectives for max version
     *
     * @return a list of organisation objectives for max version
     */
    List<OrgObjective> getAll();

    /**
     * Returns a list of published organisation objectives
     *
     * @return a list of organisation objectives
     */
    List<OrgObjective> getAllPublished();

    /**
     * Creates an organisation objective
     *
     * @param orgObjective an organisation objective
     * @return number of created organisation objectives
     */
    int create(@Param("orgObjective") OrgObjective orgObjective);

    /**
     * Delete an organisation objective
     *
     * @param uuid an identifier
     * @return number of deleted organisation objectives
     */
    int delete(@Param("uuid") UUID uuid);

    /**
     * Returns max version of organisation objectives
     *
     * @return max version of organisation objectives
     */
    int getMaxVersion();

    /**
     * Publish organisation objectives
     *
     * @return number of published organisation objectives
     */
    int publish();

    /**
     * Un-publish organisation objectives
     *
     * @return number of un-published organisation objectives
     */
    int unpublish();

}
