package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.dao.ColleagueDAO;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.util.ColleagueFactsApiLocalMapper;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.colleague.ColleagueApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
//todo @CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ConfigEntryDAO configEntryDAO;
    private final ColleagueDAO colleagueDAO;
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

    @Override
    public Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        com.tesco.pma.organisation.api.Colleague oc = configEntryDAO.getColleague(colleagueUuid);
        //todo try to download and insert colleagueApiService.findColleagueByUuid(colleagueUuid)
        return oc != null ? colleagueFactsApiLocalMapper.localToColleagueFactsApi(oc, colleagueUuid, false) : null;
    }

    @Override
    // TODO To optimize logic for update only changed attributes
    public int updateColleague(UUID colleagueUuid, Collection<String> changedAttributes) {
        int updated = 0;

        com.tesco.pma.organisation.api.Colleague existingLocalColleague = configEntryDAO.getColleague(colleagueUuid);
        Colleague colleague = colleagueApiService.findColleagueByUuid(colleagueUuid);
        if (existingLocalColleague == null || colleague == null) {
            return updated;
        }

        try {
            com.tesco.pma.organisation.api.Colleague changedLocalColleague = colleagueFactsApiLocalMapper.colleagueFactsApiToLocal(colleague);
            updateDictionaries(existingLocalColleague, changedLocalColleague);
            updated = colleagueDAO.update(changedLocalColleague);
        } catch (DataIntegrityViolationException exception) {
            String message = String.format("Data integrity violation exception = %s", exception.getMessage());
            log.error(LogFormatter.formatMessage(ErrorCodes.DATA_INTEGRITY_VIOLATION_EXCEPTION, message));
        }

        return updated;
    }

    private List<TypedAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private void updateDictionaries(com.tesco.pma.organisation.api.Colleague existingLocalColleague,
                                    com.tesco.pma.organisation.api.Colleague changedLocalColleague) {
        // Country
        com.tesco.pma.organisation.api.Colleague.Country changedCountry = changedLocalColleague.getCountry();
        if (changedCountry != null && !existingLocalColleague.getCountry().getCode().equals(changedCountry.getCode())) {
            colleagueDAO.insertCountry(changedCountry);
        }

        // Department
        com.tesco.pma.organisation.api.Colleague.Department changedDepartment = changedLocalColleague.getDepartment();
        if (changedDepartment != null && !existingLocalColleague.getDepartment().getId().equals(changedDepartment.getId())) {
            colleagueDAO.insertDepartment(changedDepartment);
        }

        // Job
        com.tesco.pma.organisation.api.Colleague.Job changedJob = changedLocalColleague.getJob();
        if (changedJob != null && !existingLocalColleague.getJob().getCode().equals(changedJob.getId())) {
            colleagueDAO.insertJob(changedJob);
        }

        // Work level
        com.tesco.pma.organisation.api.Colleague.WorkLevel changedWorkLevel = changedLocalColleague.getWorkLevel();
        if (changedWorkLevel != null && !existingLocalColleague.getWorkLevel().getCode().equals(changedWorkLevel.getCode())) {
            colleagueDAO.insertWorkLevel(changedWorkLevel);
        }
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
