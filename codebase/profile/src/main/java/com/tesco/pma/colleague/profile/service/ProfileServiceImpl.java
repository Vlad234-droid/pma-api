package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.rest.model.AggregatedColleagueResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.ColleagueResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.contact.AddressListResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.contact.ContactResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.profile.ProfileResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.service.ServiceDatesResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.workrelationships.DepartmentResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.workrelationships.JobResponse;
import com.tesco.pma.colleague.profile.rest.model.colleague.workrelationships.WorkRelationshipResponse;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.service.colleague.ColleagueApiService;
import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.Contact;
import com.tesco.pma.service.colleague.client.model.Profile;
import com.tesco.pma.service.colleague.client.model.service.ServiceDates;
import com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROFILE_ATTRIBUTE_NAME_PARAMETER_NAME = "profileAttributeName";

    @Override
    public Optional<AggregatedColleagueResponse> findProfileByColleagueUuid(UUID colleagueUuid) {

        List<ProfileAttribute> profileAttributes = findProfileAttributes(colleagueUuid);

        Colleague colleague = findColleagueByColleagueUuid(colleagueUuid);

        Colleague lineManger = findLineManager(colleague);

        return Optional.of(fillProfileResponse(colleague, lineManger, profileAttributes));

    }

    @Override
    @Transactional
    public List<ProfileAttribute> updateProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
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
    public List<ProfileAttribute> createProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            try {
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
    public List<ProfileAttribute> deleteProfileAttributes(List<ProfileAttribute> profileAttributes) {
        List<ProfileAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            if (1 == profileAttributeDAO.delete(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    private AggregatedColleagueResponse fillProfileResponse(final Colleague colleague, final Colleague lineManager,
                                                            final List<ProfileAttribute> profileAttributes) {

        AggregatedColleagueResponse aggregatedColleagueResponse = new AggregatedColleagueResponse();

        aggregatedColleagueResponse.setColleague(convertToColleagueResponse(colleague));

        aggregatedColleagueResponse.setLineManager(convertToColleagueResponse(lineManager));

        aggregatedColleagueResponse.setProfileAttributes(profileAttributes);

        return aggregatedColleagueResponse;
    }

    private ColleagueResponse convertToColleagueResponse(Colleague colleague) {
        ColleagueResponse colleagueResponse = new ColleagueResponse();

        // Common information
        colleagueResponse.setColleagueUUID(colleague.getColleagueUUID());
        colleagueResponse.setEmployeeId(colleague.getEmployeeId());
        colleagueResponse.setCountryCode(colleague.getCountryCode());

        // Profile information
        colleagueResponse.setProfile(convertToProfileResponse(colleague.getProfile()));

        // Contact information
        colleagueResponse.setContact(convertToContactResponse(colleague.getContact()));

        // Service Dates information
        colleagueResponse.setServiceDates(convertToServiceDatesResponse(colleague.getServiceDates()));

        // WorkRelationship information
        colleagueResponse.setWorkRelationships(convertToWorkRelationshipsResponse(colleague.getWorkRelationships()));

        return colleagueResponse;
    }

    private ProfileResponse convertToProfileResponse(Profile profile) {
        return Optional.ofNullable(profile)
                .map(input -> {
                    var profileResponse = new ProfileResponse();
                    BeanUtils.copyProperties(input, profileResponse);
                    return profileResponse;
                }).orElse(null);
    }

    private ContactResponse convertToContactResponse(Contact contact) {
        return Optional.ofNullable(contact)
                .map(input -> {
                    var contactResponse = new ContactResponse();
                    BeanUtils.copyProperties(input, contactResponse);

                    if (Objects.nonNull(input.getAddresses())) {
                        var addressListResponse = new AddressListResponse();
                        BeanUtils.copyProperties(input.getAddresses(), addressListResponse);
                        contactResponse.setAddresses(addressListResponse);
                    }

                    return contactResponse;
                }).orElse(null);
    }

    private ServiceDatesResponse convertToServiceDatesResponse(ServiceDates serviceDates) {
        return Optional.ofNullable(serviceDates)
                .map(input -> {
                    var serviceDatesResponse = new ServiceDatesResponse();
                    BeanUtils.copyProperties(input, serviceDatesResponse);
                    return serviceDatesResponse;
                }).orElse(null);

    }

    private List<WorkRelationshipResponse> convertToWorkRelationshipsResponse(List<WorkRelationship> workRelationships) {
        if (Objects.isNull(workRelationships)) {
            return null;
        }

        Optional<WorkRelationship> optionalWorkRelationship = workRelationships.stream()
                .filter(IS_WORK_RELATIONSHIP_ACTIVE)
                .findFirst();

        return optionalWorkRelationship
                .map(input -> {
                    var workRelationshipResponse = new WorkRelationshipResponse();
                    workRelationshipResponse.setEmploymentType(input.getEmploymentType());
                    workRelationshipResponse.setWorkSchedule(input.getWorkSchedule()); // Time Type
                    workRelationshipResponse.setEmploymentType(input.getEmploymentType());
                    workRelationshipResponse.setIsManager(input.getIsManager());

                    // Job Title
                    var jobResponse = Optional.ofNullable(input.getJob()).map(job -> {
                        var result = new JobResponse();
                        BeanUtils.copyProperties(job, result);
                        return result;
                    }).orElse(null);
                    workRelationshipResponse.setJob(jobResponse);

                    // Function
                    var departmentResponse = Optional.ofNullable(input.getDepartment()).map(department -> {
                        var result = new DepartmentResponse();
                        BeanUtils.copyProperties(department, result);
                        return result;
                    }).orElse(null);
                    workRelationshipResponse.setDepartment(departmentResponse);

                    return workRelationshipResponse;
                })
                .stream()
                .collect(Collectors.toList());

    }

    private Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
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
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(), messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
