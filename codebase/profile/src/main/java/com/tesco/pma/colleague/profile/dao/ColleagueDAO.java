package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.organisation.api.Colleague;
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
    int update(@Param("colleague") Colleague colleague);

    /**
     * Insert new job or update exists job
     *
     * @param job
     * @return number of inserted / updated jobs
     */
    int insertJob(@Param("job") Colleague.Job job);

    /**
     * Insert new country or update exists country
     *
     * @param country
     * @return number of inserted / updated countries
     */
    int insertCountry(@Param("country") Colleague.Country country);

    /**
     * Insert new work level or update exists work level
     *
     * @param workLevel
     * @return number of inserted / updated work levels
     */
    int insertWorkLevel(@Param("workLevel") Colleague.WorkLevel workLevel);

    /**
     * Insert new department or update exists department
     *
     * @param department
     * @return number of inserted / updated departments
     */
    int insertDepartment(@Param("department") Colleague.Department department);

}
