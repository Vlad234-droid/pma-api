package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface to perform database operation on colleague
 */
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
     * Save work level into DB, if object already exists update it
     *
     * @param workLevel - object to be saved
     * @return number of inserted / updated work levels
     */
    int updateWorkLevel(@Param("workLevel") ColleagueEntity.WorkLevel workLevel);

    /**
     * Save country into DB, if object already exists do update it
     *
     * @param country - object to be saved
     * @return number of inserted / updated countries
     */
    int updateCountry(@Param("country") ColleagueEntity.Country country);

    /**
     * Save department into DB, if object already exists update it
     *
     * @param department - object to be saved
     * @return number of inserted / updated departments
     */
    int updateDepartment(@Param("department") ColleagueEntity.Department department);

    /**
     * Save business type into DB, if object already exists update it
     *
     * @param businessType - object to be saved
     * @return number of inserted / updated business types
     */
    int updateBusinessType(@Param("businessType") ColleagueEntity.Department.BusinessType businessType);

    /**
     * Save job into DB, if object already exists update it
     *
     * @param job - object to be saved
     * @return number of inserted / updated jobs
     */
    int updateJob(@Param("job") ColleagueEntity.Job job);

    /**
     * Save colleague into DB
     *
     * @param colleague - object to be saved
     * @return number of inserted records
     */
    int saveColleague(@Param("colleague") ColleagueEntity colleague);

    /**
     * Update colleague in DB
     *
     * @param colleague - object to be updated
     * @return number of updated records
     */
    int updateColleague(@Param("colleague") ColleagueEntity colleague);

    /**
     * Update manager by colleague uuid
     *
     * @param colleagueUuid - colleague identifier
     * @param managerUuid   - manager identifier
     * @return number of inserted / updated records
     */
    int updateColleagueManager(@Param("colleagueUuid") UUID colleagueUuid, @Param("managerUuid") UUID managerUuid);

    /**
     * Check is colleague exists into DB
     *
     * @param colleagueUuid - colleague identifier
     * @return true/false
     */
    boolean isColleagueExists(@Param("uuid") UUID colleagueUuid);

    /**
     * Check if uuid from set is present into db
     *
     * @param colleagueUuids - set of colleague uuid to check
     * @return uuid if present in DB, otherwise skip it
     */
    Set<UUID> getAllColleaguesUuids(@Param("colleagueUuids") Collection<UUID> colleagueUuids);

    /**
     * Get colleague suggestions by full name
     *
     * @param requestQuery
     * @return list colleagues
     */
    List<Colleague> findColleagueSuggestionsByFullName(@Param("requestQuery") RequestQuery requestQuery);

    /**
     * Find all departments
     *
     * @return list of departments
     */
    List<ColleagueEntity.Department> findAllDepartments();

    /**
     * Try to find department by main properties
     *
     * @param id           - external identifier
     * @param name         - name
     * @param businessType - business type name
     * @return department
     */
    ColleagueEntity.Department findDepartment(@Param("id") String id,
                                              @Param("name") String name,
                                              @Param("businessType") String businessType);

    /**
     * Find business type by its name
     *
     * @param name - name
     * @return business type
     */
    ColleagueEntity.Department.BusinessType findBusinessType(@Param("name") String name);
}
