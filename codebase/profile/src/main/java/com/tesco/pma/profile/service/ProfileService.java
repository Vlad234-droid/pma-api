package com.tesco.pma.profile.service;

import com.tesco.pma.profile.domain.Profile;

import javax.validation.constraints.NotNull;
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
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<Profile> findProfileByColleagueUuid(@NotNull UUID colleagueUuid);

}
