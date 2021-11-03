package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;

import javax.validation.constraints.NotNull;
import java.util.Collection;
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
    Optional<ColleagueProfile> findProfileByColleagueUuid(@NotNull UUID colleagueUuid);

    /**
     * Update profile attributes
     *
     * @param profileAttributes
     * @return Updated profile attributes
     */
    List<TypedAttribute> updateProfileAttributes(@NotNull UUID colleagueUuid, List<TypedAttribute> profileAttributes);

    /**
     * Insert profile attributes
     *
     * @param profileAttributes
     * @return Inserted profile attributes
     */
    List<TypedAttribute> createProfileAttributes(@NotNull UUID colleagueUuid, List<TypedAttribute> profileAttributes);

    /**
     * Delete profile attributes
     *
     * @param profileAttributes
     * @return Deleted profile attributes
     */
    List<TypedAttribute> deleteProfileAttributes(@NotNull UUID colleagueUuid, List<TypedAttribute> profileAttributes);

    /**
     * Update colleague changed attributes
     *
     * @param colleagueUuid
     * @param changedAttributes
     * @return Number of updated records
     */
    int updateColleague(@NotNull UUID colleagueUuid, Collection<String> changedAttributes);

}
