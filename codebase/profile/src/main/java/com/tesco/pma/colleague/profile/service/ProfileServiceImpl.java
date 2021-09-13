package com.tesco.pma.colleague.profile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.service.colleague.ColleagueApiService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
//todo @CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ProfileAttributeDAO profileAttributeDAO;
    private final ColleagueApiService colleagueApiService;
    private final NamedMessageSourceAccessor messages;

    private static final Predicate<com.tesco.pma.colleague.api.workrelationships.WorkRelationship>
            IS_WORK_RELATIONSHIP_ACTIVE = workRelationship -> workRelationship.getWorkingStatus().equals(
                    com.tesco.pma.colleague.api.workrelationships.WorkRelationship.WorkingStatus.ACTIVE);

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROFILE_ATTRIBUTE_NAME_PARAMETER_NAME = "profileAttributeName";

    @Override
    //todo    @Cacheable
    public Optional<ColleagueProfile> findProfileByColleagueUuid(UUID colleagueUuid) {

        var colleague = findColleagueByColleagueUuid(colleagueUuid);
        if (colleague != null) {
            var colleagueProfile = new ColleagueProfile();
            colleagueProfile.setColleague(colleague);

            var profileAttributes = findProfileAttributes(colleagueUuid);
            if (!profileAttributes.isEmpty()) {
                colleagueProfile.setProfileAttributes(profileAttributes);
            }
            return Optional.of(colleagueProfile);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public List<TypedAttribute> updateProfileAttributes(UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            profileAttribute.setColleagueUuid(colleagueUuid);
            if (1 == profileAttributeDAO.update(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    @Override
    @Transactional
    public List<TypedAttribute> createProfileAttributes(UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            try {
                profileAttribute.setColleagueUuid(colleagueUuid);
                if (1 == profileAttributeDAO.create(profileAttribute)) {
                    results.add(profileAttribute);
                } else {
                    throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid());
                }
            } catch (DuplicateKeyException e) {
                throw new DatabaseConstraintViolationException(ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS.name(),
                        messages.getMessage(ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS,
                                Map.of(PROFILE_ATTRIBUTE_NAME_PARAMETER_NAME, profileAttribute.getName(),
                                        COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid()
                                )), null, e);

            }

        });
        return results;
    }

    @Override
    public List<TypedAttribute> deleteProfileAttributes(final UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            profileAttribute.setColleagueUuid(colleagueUuid);
            if (1 == profileAttributeDAO.delete(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    private Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        return colleagueApiService.findColleagueByUuid(colleagueUuid);
    }

    private List<TypedAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
