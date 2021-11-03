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

}
