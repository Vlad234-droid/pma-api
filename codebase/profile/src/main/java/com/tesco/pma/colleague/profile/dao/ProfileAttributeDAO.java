package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on profile
 */
public interface ProfileAttributeDAO {

    /**
     * Returns a list of profile attributes
     *
     * @param colleagueUuid an identifier
     * @return a list of profile attributes
     */
    List<TypedAttribute> get(@Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Update a profile attribute
     *
     * @param profileAttribute a Profile attribute
     * @return number of updated profileAttributes
     */
    int update(@Param("profileAttribute") TypedAttribute profileAttribute);

    /**
     * Insert a profile attribute
     *
     * @param profileAttribute a Profile attribute
     * @return number of inserted profileAttributes
     */
    int create(@Param("profileAttribute") TypedAttribute profileAttribute);

    /**
     * Delete a profile attribute
     *
     * @param profileAttribute a Profile attribute
     * @return number of deleted Profile attributes
     */
    int delete(@Param("profileAttribute") TypedAttribute profileAttribute);

}
