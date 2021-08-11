package com.tesco.pma.profile.dao;

import com.tesco.pma.profile.domain.Profile;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/**
 * Interface to perform database operation on profile
 */
public interface ProfileDAO {

    /**
     * Returns a profile
     *
     * @param colleagueUuid an identifier
     * @return a Profile
     */
    Profile get(@Param("colleagueUuid") UUID colleagueUuid);

}
