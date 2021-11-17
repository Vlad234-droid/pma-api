package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

public interface ProfileDAO {

    /**
     * Get colleague by iam id
     *
     * @param colleagueUuid colleague identifier
     * @return colleague object
     */
    ColleagueEntity getColleague(@Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Get colleague by iam id
     *
     * @param iamId colleague iam identifier
     * @return colleague object
     */
    ColleagueEntity getColleagueByIamId(@Param("iamId") String iamId);

    /**
     * Save work level into DB, if object already exists do nothing
     *
     * @param workLevel - object to be saved
     */
    void saveWorkLevel(@Param("workLevel") ColleagueEntity.WorkLevel workLevel);

    /**
     * Save country into DB, if object already exists do nothing
     *
     * @param country - object to be saved
     */
    void saveCountry(@Param("country") ColleagueEntity.Country country);

    /**
     * Save department into DB, if object already exists do nothing
     *
     * @param department - object to be saved
     */
    void saveDepartment(@Param("department") ColleagueEntity.Department department);

    /**
     * Save job into DB, if object already exists do nothing
     *
     * @param job - object to be saved
     */
    void saveJob(@Param("job") ColleagueEntity.Job job);

    /**
     * Save colleague into DB, if object already exists update it
     *
     * @param colleague - object to be saved
     */
    void saveColleague(@Param("colleague") ColleagueEntity colleague);

    /**
     * Update manager by colleague uuid
     *
     * @param colleagueUuid - colleague identifier
     * @param managerUuid   - manager identifier
     */
    void updateColleagueManager(@Param("colleagueUuid") UUID colleagueUuid, @Param("managerUuid") UUID managerUuid);

    /**
     * Check is colleague exists into DB
     *
     * @param colleagueUuid - colleague identifier
     * @return true/false
     */
    boolean isColleagueExists(@Param("uuid") UUID colleagueUuid);
}
