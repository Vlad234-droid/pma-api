package com.tesco.pma.profile.service;

import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.Profile;

import javax.validation.constraints.NotNull;
import java.util.List;
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

    List<ProfileAttribute> updateProfileAttributes(UUID colleagueUuid, List<ProfileAttribute> profileAttributes);

}
