package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.apache.ibatis.annotations.Param;

/**
 * Interface to perform database operation on colleague
 */
public interface ColleagueDAO {

    /**
     * Update a Colleague attributes
     *
     * @param colleague a Colleague attribute
     * @return number of updated colleagues
     */
    int update(@Param("colleague") ColleagueEntity colleague);

    /**
     * Insert new job or update exists job
     *
     * @param job
     * @return number of inserted / updated jobs
     */
    int insertJob(@Param("job") ColleagueEntity.Job job);

    /**
     * Insert new country or update exists country
     *
     * @param country
     * @return number of inserted / updated countries
     */
    int insertCountry(@Param("country") ColleagueEntity.Country country);

    /**
     * Insert new work level or update exists work level
     *
     * @param workLevel
     * @return number of inserted / updated work levels
     */
    int insertWorkLevel(@Param("workLevel") ColleagueEntity.WorkLevel workLevel);

    /**
     * Insert new department or update exists department
     *
     * @param department
     * @return number of inserted / updated departments
     */
    int insertDepartment(@Param("department") ColleagueEntity.Department department);

}
