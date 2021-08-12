package com.tesco.pma.profile.dao;

import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.Profile;
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
    List<ProfileAttribute> get(@Param("colleagueUuid") UUID colleagueUuid);

}
