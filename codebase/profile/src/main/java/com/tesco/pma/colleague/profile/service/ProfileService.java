package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.colleague.profile.service.rest.model.AggregatedColleagueResponse;

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
    Optional<AggregatedColleagueResponse> findProfileByColleagueUuid(@NotNull UUID colleagueUuid);

    /**
     * Update profile attributes
     *
     * @param profileAttributes
     * @return Updated profile attributes
     */
    List<ProfileAttribute> updateProfileAttributes(@NotNull UUID colleagueUuid, List<ProfileAttribute> profileAttributes);

    /**
     * Insert profile attributes
     *
     * @param profileAttributes
     * @return Inserted profile attributes
     */
    List<ProfileAttribute> createProfileAttributes(@NotNull UUID colleagueUuid, List<ProfileAttribute> profileAttributes);

    /**
     * Delete profile attributes
     *
     * @param profileAttributes
     * @return Deleted profile attributes
     */
    List<ProfileAttribute> deleteProfileAttributes(@NotNull UUID colleagueUuid, List<ProfileAttribute> profileAttributes);

}
