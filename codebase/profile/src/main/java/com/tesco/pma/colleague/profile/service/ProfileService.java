package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;

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
     * Find colleague by id
     *
     * @param colleagueUuid
     * @return Deleted profile attributes
     */
    Colleague findColleagueByColleagueUuid(UUID colleagueUuid);

    /**
     * Get colleague by IAM identifier
     *
     * @param iamId - iam identifier
     * @return colleague object
     */
    ColleagueEntity getColleagueByIamId(String iamId);

}
