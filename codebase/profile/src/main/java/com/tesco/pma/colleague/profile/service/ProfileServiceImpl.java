package com.tesco.pma.colleague.profile.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.parser.XlsxParser;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.Job;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
//todo @CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ProfileDAO profileDAO;
    private final ProfileAttributeDAO profileAttributeDAO;
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

    @Override
    public ColleagueEntity getColleagueByIamId(String iamId) {
        return profileDAO.getColleagueByIamId(iamId);
    }

    @Override
    public ImportReport importColleagues(InputStream inputStream) {
        var parser = new XlsxParser();
        var result = parser.parse(inputStream);
        var workLevels = ColleagueMapper.mapWLs(result.getData());
        var countries = ColleagueMapper.mapCountries(result.getData());
        var departments = ColleagueMapper.mapDepartments(result.getData());
        var jobs = ColleagueMapper.mapJobs(result.getData());
        var colleagues = ColleagueMapper.mapColleagues(result.getData(), workLevels, countries, departments, jobs);
        workLevels.forEach(profileDAO::saveWorkLevel);
        countries.forEach(profileDAO::saveCountry);
        departments.forEach(profileDAO::saveDepartment);
        jobs.forEach(profileDAO::saveJob);

        return saveColleagues(colleagues);
    }

    private ImportReport saveColleagues(List<ColleagueEntity> colleagues) {
        var uuidToManager = colleagues.stream()
                .filter(c -> c.getManagerUuid() != null)
                .collect(Collectors.toMap(ColleagueEntity::getUuid, ColleagueEntity::getManagerUuid));
        var builder = ImportReport.builder();
        colleagues.forEach(c -> {
            c.setManagerUuid(null);
            profileDAO.saveColleague(c);
            builder.importColleague(c.getUuid());
        });
        for (var colleague : uuidToManager.entrySet()) {
            if (profileDAO.isColleagueExists(colleague.getValue())) {
                profileDAO.updateColleagueManager(colleague.getKey(), colleague.getValue());
            } else {
                builder.usersManagerSkip(colleague.getKey());
            }
        }
        return builder.build();
    }

    private Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        ColleagueEntity oc = profileDAO.getColleague(colleagueUuid);
        //todo try to download and insert colleagueApiService.findColleagueByUuid(colleagueUuid)
        return oc != null ? getColleague(oc, colleagueUuid) : null;
    }

    private Colleague getColleague(ColleagueEntity oc, UUID colleagueUuid) {
        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUuid);
        colleague.setCountryCode(oc.getCountry().getCode());
        colleague.setProfile(getProfile(oc));
        colleague.setWorkRelationships(Collections.singletonList(getWorkRelationship(oc)));
        colleague.setExternalSystems(getExternalSystems(oc));
        colleague.setContact(getContact(oc));
        return colleague;
    }

    private Contact getContact(ColleagueEntity oc) {
        if (oc.getEmail() != null) {
            var contact = new Contact();
            contact.setEmail(oc.getEmail());
            return contact;
        }
        return null;
    }

    private ExternalSystems getExternalSystems(ColleagueEntity oc) {
        var es = new ExternalSystems();
        var iam = new IamSourceSystem();
        iam.setId(oc.getIamId());
        iam.setSource(oc.getIamSource());
        es.setIam(iam);
        return es;
    }

    private Profile getProfile(ColleagueEntity oc) {
        var profile = new Profile();
        profile.setFirstName(oc.getFirstName());
        profile.setMiddleName(oc.getMiddleName());
        profile.setLastName(oc.getLastName());
        return profile;
    }

    private WorkRelationship getWorkRelationship(ColleagueEntity oc) {
        var wr = new WorkRelationship();
        wr.setWorkLevel(WorkRelationship.WorkLevel.getByCode(oc.getWorkLevel().getCode()));
        wr.setPrimaryEntity(oc.getPrimaryEntity());
        wr.setSalaryFrequency(oc.getSalaryFrequency());
        wr.setDepartment(getDepartment(oc));
        wr.setJob(getJob(oc));
        return wr;
    }

    private Job getJob(ColleagueEntity oc) {
        var ocJob = oc.getJob();
        if (ocJob != null) {
            var job = new Job();
            job.setId(ocJob.getId());
            job.setName(ocJob.getName());
            job.setCode(ocJob.getCode());
            job.setCostCategory(ocJob.getCostCategory());
            return job;
        }
        return null;
    }

    private Department getDepartment(ColleagueEntity oc) {
        var ocDp = oc.getDepartment();
        if (ocDp != null) {
            var dp = new Department();
            dp.setId(ocDp.getId());
            dp.setName(ocDp.getName());
            dp.setBusinessType(ocDp.getBusinessType());
            return dp;
        }
        return null;
    }

    private List<TypedAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
