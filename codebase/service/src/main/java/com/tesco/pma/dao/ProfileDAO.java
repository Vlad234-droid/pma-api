package com.tesco.pma.dao;

import com.tesco.pma.api.Profile;

import java.util.List;

/**
 * Interface to perform database operation on profile
 */
public interface ProfileDAO {

    /**
     * Returns all profiles
     *
     * @return a list of Profiles
     */
    List<Profile> getAll();

}
