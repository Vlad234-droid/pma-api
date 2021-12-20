package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.util.ColleagueFactsApiLocalMapper;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.service.colleague.ColleagueApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_NOT_FOUND;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
//todo @CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ProfileDAO profileDAO;
    private final ProfileAttributeDAO profileAttributeDAO;
    private final ColleagueApiService colleagueApiService;
    private final NamedMessageSourceAccessor messages;
    private final ColleagueFactsApiLocalMapper colleagueFactsApiLocalMapper;

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROFILE_ATTRIBUTE_NAME_PARAMETER_NAME = "profileAttributeName";

    @Override
    //todo    @Cacheable
    public Optional<ColleagueProfile> findProfileByColleagueUuid(UUID colleagueUuid) {

        var colleague = findColleagueByColleagueUuid(colleagueUuid);
        if (colleague != null) {
            return Optional.of(prepareColleagueProfile(colleague));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ColleagueProfile> findProfileByColleagueIamId(String iamId) {
        var colleagueEntity = profileDAO.getColleagueByIamId(iamId);
        if (colleagueEntity == null) {
            return Optional.empty();
        }

        var colleague = colleagueFactsApiLocalMapper.localToColleagueFactsApi(colleagueEntity, colleagueEntity.getUuid(), false);
        if (colleague != null) {
            return Optional.of(prepareColleagueProfile(colleague));
        }

        return Optional.empty();
    }

    private ColleagueProfile prepareColleagueProfile(Colleague colleague) {
        var colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(colleague);

        var profileAttributes = findProfileAttributes(colleague.getColleagueUUID());
        if (!profileAttributes.isEmpty()) {
            colleagueProfile.setProfileAttributes(profileAttributes);
        }
        return colleagueProfile;
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

    @Override
    public ColleagueEntity getColleagueByIamId(String iamId) {
        return profileDAO.getColleagueByIamId(iamId);
    }

    @Override
    public Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        var oc = profileDAO.getColleague(colleagueUuid);
        //todo try to download and insert colleagueApiService.findColleagueByUuid(colleagueUuid)
        return oc != null ? colleagueFactsApiLocalMapper.localToColleagueFactsApi(oc, colleagueUuid, false) : null;
    }

    @Override
    public ColleagueEntity getColleague(UUID colleagueUuid) {
        var colleague = profileDAO.getColleague(colleagueUuid);
        if (colleague == null) {
            throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid);
        }
        return colleague;
    }

    @Override
    public List<ColleagueProfile> getSuggestions(RequestQuery requestQuery) {

        return profileDAO.findColleagueSuggestionsByFullName(requestQuery).stream()
                .map(colleague -> {
                    var colleagueProfile = new ColleagueProfile();
                    colleagueProfile.setColleague(colleague);
                    colleagueProfile.setProfileAttributes(this.findProfileAttributes(colleague.getColleagueUUID()));
                    return colleagueProfile;
                }).collect(Collectors.toList());
    }

    @Override
    // TODO To optimize logic for update only changed fields
    public int updateColleague(UUID colleagueUuid, Collection<String> changedFields) {
        int updated = 0;

        var existingLocalColleague = profileDAO.getColleague(colleagueUuid);
        if (existingLocalColleague == null) {
            return updated;
        }

        Colleague colleague = colleagueApiService.findColleagueByUuid(colleagueUuid);
        if (colleague == null) {
            return updated;
        }

        return persistColleague(colleague, existingLocalColleague);
    }

    @Override
    public int create(UUID colleagueUuid) {
        var existingLocalColleague = profileDAO.getColleague(colleagueUuid);
        if (existingLocalColleague != null) {
            return updateColleague(colleagueUuid, List.of());
        }

        Colleague colleague = colleagueApiService.findColleagueByUuid(colleagueUuid);
        if (colleague == null) {
            return 0;
        }

        return persistColleague(colleague, null);

    }

    private int persistColleague(Colleague colleague, ColleagueEntity existingLocalColleague) {
        int updated = 0;

        try {
            ColleagueEntity changedLocalColleague = colleagueFactsApiLocalMapper.colleagueFactsApiToLocal(colleague);
            if (existingLocalColleague == null) {
                updated = profileDAO.saveColleague(changedLocalColleague);
            } else {
                updated = profileDAO.updateColleague(changedLocalColleague);
            }
            updateDictionaries(existingLocalColleague, changedLocalColleague);
        } catch (DataIntegrityViolationException exception) {
            String message = String.format("Data integrity violation exception = %s", exception.getMessage());
            log.error(LogFormatter.formatMessage(ErrorCodes.DATA_INTEGRITY_VIOLATION_EXCEPTION, message));
        }

        return updated;
    }

    private List<TypedAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private void updateDictionaries(ColleagueEntity existingLocalColleague,
                                    ColleagueEntity changedLocalColleague) {
        // Country
        updateCountryDictionary(existingLocalColleague, changedLocalColleague);

        // Department
        updateDepartmentDictionary(existingLocalColleague, changedLocalColleague);

        // Job
        updateJobDictionary(existingLocalColleague, changedLocalColleague);

        // Work level
        updateWorkLevelDictionary(existingLocalColleague, changedLocalColleague);
    }

    private void updateCountryDictionary(ColleagueEntity existingLocalColleague, ColleagueEntity changedLocalColleague) {
        ColleagueEntity.Country changedCountry = changedLocalColleague.getCountry();
        if (changedCountry != null && (existingLocalColleague == null
                || !existingLocalColleague.getCountry().getCode().equals(changedCountry.getCode()))) {
            profileDAO.updateCountry(changedCountry);
        }
    }

    private void updateDepartmentDictionary(ColleagueEntity existingLocalColleague, ColleagueEntity changedLocalColleague) {
        ColleagueEntity.Department changedDepartment = changedLocalColleague.getDepartment();
        if (changedDepartment != null && (existingLocalColleague == null
                || !existingLocalColleague.getDepartment().getId().equals(changedDepartment.getId()))) {
            profileDAO.updateDepartment(changedDepartment);
        }
    }

    private void updateJobDictionary(ColleagueEntity existingLocalColleague, ColleagueEntity changedLocalColleague) {
        ColleagueEntity.Job changedJob = changedLocalColleague.getJob();
        if (changedJob != null && (existingLocalColleague == null
                || !existingLocalColleague.getJob().getCode().equals(changedJob.getId()))) {
            profileDAO.updateJob(changedJob);
        }
    }

    private void updateWorkLevelDictionary(ColleagueEntity existingLocalColleague, ColleagueEntity changedLocalColleague) {
        ColleagueEntity.WorkLevel changedWorkLevel = changedLocalColleague.getWorkLevel();
        if (changedWorkLevel != null && (existingLocalColleague == null
                || !existingLocalColleague.getWorkLevel().getCode().equals(changedWorkLevel.getCode()))) {
            profileDAO.updateWorkLevel(changedWorkLevel);
        }
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(PROFILE_NOT_FOUND.getCode(),
                messages.getMessage(PROFILE_NOT_FOUND,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
