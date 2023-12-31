package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.pagination.RequestQuery;

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
     * Finds profile by colleague IAM Id.
     *
     * @param iamId colleague iamId, not null.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<ColleagueProfile> findProfileByColleagueIamId(@NotNull String iamId);

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
     * Update colleague changed fields
     * Colleague Facts API will be use
     *
     * @param colleagueUuid
     * @param changedFields
     * @return Number of updated records
     */
    int updateColleague(@NotNull UUID colleagueUuid, Collection<String> changedFields);

    /**
     * Update an existing colleague
     *
     * @param colleague
     * @return Number of updated records
     */
    int updateColleague(@NotNull Colleague colleague);

    /**
     * Save a joiner colleague
     * Colleague Facts API will be use
     *
     * @param colleagueUuid
     * @return Number of updated records
     */
    int create(@NotNull UUID colleagueUuid);

    /**
     * Save a joiner colleague
     *
     * @param colleague
     * @return Number of updated records
     */
    int create(@NotNull Colleague colleague);

    /**
     * Find colleague by uuid
     *
     * @param colleagueUuid - colleague identifier
     * @return colleague object
     */
    Colleague findColleagueByColleagueUuid(UUID colleagueUuid);

    /**
     * Get colleague by IAM identifier
     *
     * @param iamId - iam identifier
     * @return colleague object
     */
    ColleagueEntity getColleagueByIamId(String iamId);

    /**
     * Get colleague by UUID
     *
     * @param colleagueUuid - colleague identifier
     * @return colleague object
     * @throws com.tesco.pma.exception.NotFoundException if colleague is not present into DB
     */
    ColleagueEntity getColleague(UUID colleagueUuid);

    /**
     * Search colleagues
     *
     * @param requestQuery
     * @return colleagues list
     */
    List<ColleagueProfile> getSuggestions(RequestQuery requestQuery);

    /**
     * Get a list supported attributes of Colleague Facts API
     *
     * @return list of supported attributes
     */
    Collection<String> getColleagueFactsAPISupportedAttributes();

}
