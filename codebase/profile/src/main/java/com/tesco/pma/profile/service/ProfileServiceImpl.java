package com.tesco.pma.profile.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.ProfileResponse;
import com.tesco.pma.service.colleague.ColleagueApiService;
import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.workrelationships.Department;
import com.tesco.pma.service.colleague.client.model.workrelationships.Job;
import com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.tesco.pma.profile.exception.ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS;
import static com.tesco.pma.profile.exception.ErrorCodes.PROFILE_NOT_FOUND;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileAttributeDAO profileAttributeDAO;
    private final ColleagueApiService colleagueApiService;
    private final NamedMessageSourceAccessor messages;

    private static final Predicate<WorkRelationship> IS_WORK_RELATIONSHIP_ACTIVE = workRelationship ->
            workRelationship.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE);

    @Override
    public Optional<ProfileResponse> findProfileByColleagueUuid(UUID colleagueUuid) {

        List<ProfileAttribute> profileAttributes = findProfileAttributes(colleagueUuid);

        Colleague colleague = findColleague(colleagueUuid);

        Colleague lineManger = findLineManager(colleague);

        return Optional.of(fillProfile(colleague, lineManger, profileAttributes));

    }

    @Override
    @Transactional
    public List<ProfileAttribute> updateProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            if (1 == profileAttributeDAO.update(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound("colleagueUuid", profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    @Override
    @Transactional
    public List<ProfileAttribute> createProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            try {
                if (1 == profileAttributeDAO.create(profileAttribute)) {
                    results.add(profileAttribute);
                } else {
                    throw notFound("colleagueUuid", profileAttribute.getColleagueUuid());
                }
            } catch (DuplicateKeyException e) {
                throw new DatabaseConstraintViolationException(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS.name(),
                        messages.getMessage(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS,
                                Map.of("profileAttributeName", profileAttribute.getName(),
                                        "colleagueUuid", profileAttribute.getColleagueUuid()
                                )), null, e);

            }

        });
        return results;
    }

    @Override
    public List<ProfileAttribute> deleteProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            if (1 == profileAttributeDAO.delete(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound("colleagueUuid", profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    private ProfileResponse fillProfile(final Colleague colleague, final Colleague lineManager,
                                        final List<ProfileAttribute> profileAttributes) {

        ProfileResponse profileResponse = new ProfileResponse();

        // Personal information

        final var colleagueProfile = colleague.getProfile();
        if (Objects.nonNull(colleagueProfile)) {
            BeanUtils.copyProperties(colleagueProfile, profileResponse);
        }

        // Contact

        final var contact = colleague.getContact();
        if (Objects.nonNull(contact)) {
            profileResponse.setEmailAddress(contact.getEmail());
            profileResponse.setMobilePhone(contact.getWorkPhoneNumber());
        }

        // Professional information

        final var serviceDates = colleague.getServiceDates();
        if (Objects.nonNull(serviceDates)) {
            profileResponse.setHireDate(serviceDates.getHireDate());
        }

        if (Objects.nonNull(colleague.getWorkRelationships())) {
            Optional<WorkRelationship> optionalWorkRelationship = colleague.getWorkRelationships().stream().findFirst();

            profileResponse.setEmploymentType(optionalWorkRelationship.map(WorkRelationship::getEmploymentType)
                    .orElse(null));

            profileResponse.setJobTitle(optionalWorkRelationship.flatMap(workRelationship
                            -> Optional.ofNullable(workRelationship.getJob()).map(Job::getName))
                    .orElse(null));

            profileResponse.setFunction(optionalWorkRelationship.flatMap(workRelationship
                            -> Optional.ofNullable(workRelationship.getDepartment()).map(Department::getBusinessType))
                    .orElse(null));

            profileResponse.setTimeType(
                    optionalWorkRelationship.map(WorkRelationship::getWorkSchedule).orElse(null));

        }

        if (Objects.nonNull(lineManager)) {
            Function<com.tesco.pma.service.colleague.client.model.Profile, String> fullNameMapper =
                    lineManagerProfile -> {
                        var names = new ArrayList<String>();
                        names.add(lineManagerProfile.getFirstName());
                        if (Objects.nonNull(lineManagerProfile.getMiddleName())) {
                            names.add(lineManagerProfile.getMiddleName());
                        }
                        names.add(lineManagerProfile.getLastName());
                        return String.join(" ", names);
                    };
            String fullName = Optional.ofNullable(lineManager.getProfile())
                    .map(fullNameMapper)
                    .orElse(null);
            profileResponse.setLineManager(fullName);
        }

        // Location
        if (Objects.nonNull(contact)) {
            var addresses = contact.getAddresses();
            profileResponse.setCountry(addresses.getCountryCode());
            profileResponse.setCity(addresses.getCity());
            profileResponse.setAddress(String.join(" ", addresses.getLines()));
        }

        // Profile Attributes
        if (!profileAttributes.isEmpty()) {
            BeanWrapper targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(profileResponse);
            profileAttributes.forEach(profileAttribute ->
                    {
                        String propertyName = profileAttribute.getName();
                        targetBeanWrapper.setPropertyValue(propertyName, profileAttribute.getValue());
                    }
            );
        }

        return profileResponse;
    }

    private Colleague findColleague(UUID colleagueUuid) {
        return colleagueApiService.tryFindColleagueByUuid(colleagueUuid);
    }

    private Colleague findLineManager(Colleague colleague) {
        if (Objects.isNull(colleague)) {
            return null;
        }

        Optional<UUID> managerUUID = colleague.getWorkRelationships().stream()
                .filter(IS_WORK_RELATIONSHIP_ACTIVE)
                .findFirst()
                .map(WorkRelationship::getManagerUUID);

        return managerUUID.map(colleagueApiService::tryFindColleagueByUuid).orElse(null);

    }

    private List<ProfileAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(PROFILE_NOT_FOUND.getCode(), messages.getMessage(PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
