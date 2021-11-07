package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.api.service.ServiceDates;
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
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
//todo @CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ConfigEntryDAO configEntryDAO;
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

    private Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        com.tesco.pma.organisation.api.Colleague oc = configEntryDAO.getColleague(colleagueUuid);
        //todo try to download and insert colleagueApiService.findColleagueByUuid(colleagueUuid)
        return oc != null ? getColleague(oc, colleagueUuid, false) : null;
    }

    private Colleague getColleague(com.tesco.pma.organisation.api.Colleague oc, UUID colleagueUuid, boolean child) {
        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUuid);
        colleague.setCountryCode(oc.getCountry().getCode());
        colleague.setProfile(getProfile(oc));
        colleague.setContact(getContact(oc));
        if (!child) {
            colleague.setWorkRelationships(Collections.singletonList(getWorkRelationship(oc)));
            colleague.setExternalSystems(getExternalSystems(oc));
            colleague.setServiceDates(getServiceDates(oc));
        }
        return colleague;
    }

    private ServiceDates getServiceDates(com.tesco.pma.organisation.api.Colleague oc) {
        var serviceDates = new ServiceDates();
        serviceDates.setHireDate(oc.getHireDate());
        serviceDates.setLeavingDate(oc.getLeavingDate());
        return serviceDates;
    }

    private Contact getContact(com.tesco.pma.organisation.api.Colleague oc) {
        if (oc.getEmail() != null) {
            var contact = new Contact();
            contact.setEmail(oc.getEmail());
            return contact;
        }
        return null;
    }

    private ExternalSystems getExternalSystems(com.tesco.pma.organisation.api.Colleague oc) {
        var es = new ExternalSystems();
        var iam = new IamSourceSystem();
        iam.setId(oc.getIamId());
        iam.setSource(oc.getIamSource());
        es.setIam(iam);
        return es;
    }

    private Profile getProfile(com.tesco.pma.organisation.api.Colleague oc) {
        var profile = new Profile();
        profile.setFirstName(oc.getFirstName());
        profile.setMiddleName(oc.getMiddleName());
        profile.setLastName(oc.getLastName());
        return profile;
    }

    private WorkRelationship getWorkRelationship(com.tesco.pma.organisation.api.Colleague oc) {
        var wr = new WorkRelationship();
        wr.setWorkLevel(WorkRelationship.WorkLevel.getByCode(oc.getWorkLevel().getCode()));
        wr.setPrimaryEntity(oc.getPrimaryEntity());
        wr.setSalaryFrequency(oc.getSalaryFrequency());
        wr.setIsManager(oc.isManager());
        wr.setEmploymentType(oc.getEmploymentType());
        wr.setDepartment(getDepartment(oc));
        wr.setJob(getJob(oc));
        wr.setManagerUUID(oc.getManagerUuid());
        if (wr.getManagerUUID() != null) {
            com.tesco.pma.organisation.api.Colleague mng = configEntryDAO.getColleague(wr.getManagerUUID());
            if (mng != null) {
                wr.setManager(getColleague(mng, wr.getManagerUUID(), true));
            }
        }
        return wr;
    }

    private Job getJob(com.tesco.pma.organisation.api.Colleague oc) {
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

    private Department getDepartment(com.tesco.pma.organisation.api.Colleague oc) {
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
                messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
