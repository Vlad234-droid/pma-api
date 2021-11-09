package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface ProfileDAO {

    /**
     * Gets list of colleagues by types key
     *
     * @param key - types
     * @return list of colleagues
     */
    List<ColleagueEntity> findColleaguesByTypes(@Param("key") String key);

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

    void saveWorkLevel(@Param("workLevel") ColleagueEntity.WorkLevel workLevel);

    void saveCountry(@Param("country") ColleagueEntity.Country country);

    void saveDepartment(@Param("department") ColleagueEntity.Department department);

    void saveJob(@Param("job") ColleagueEntity.Job job);

    void saveColleague(@Param("colleague") ColleagueEntity colleague);

    void updateColleagueManager(@Param("colleagueUuid") UUID colleagueUuid, @Param("managerUuid") UUID managerUuid);

    boolean isColleagueExists(@Param("uuid") UUID colleagueUuid);
}
