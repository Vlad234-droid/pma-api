package com.tesco.pma.profile.service;

import com.tesco.pma.api.Profile;
import com.tesco.pma.service.user.UserIncludes;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Profile service
 */
public interface ProfileService {

    /**
     * Finds profile by colleague uuid.
     *
     * @param colleagueUuid colleague uuid, not null.
     * @param includes      additional data to be included.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<Profile> findProfileByColleagueUuid(@NotNull UUID colleagueUuid, Collection<UserIncludes> includes);

}
