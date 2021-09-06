package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.rest.model.AggregatedColleague;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.Colleague;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.contact.AddressList;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.contact.Contact;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.profile.Profile;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.service.ServiceDates;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships.Department;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships.Job;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships.WorkRelationship;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.service.colleague.ColleagueApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(cacheNames = "aggregatedColleagues")
public class ProfileServiceImpl implements ProfileService {

    private final ProfileAttributeDAO profileAttributeDAO;
    private final ColleagueApiService colleagueApiService;
    private final NamedMessageSourceAccessor messages;

    private static final Predicate<com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship>
            IS_WORK_RELATIONSHIP_ACTIVE = workRelationship -> workRelationship.getWorkingStatus().equals(
                    com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship.WorkingStatus.ACTIVE);

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PROFILE_ATTRIBUTE_NAME_PARAMETER_NAME = "profileAttributeName";

    @Override
    @Cacheable
    public Optional<AggregatedColleague> findProfileByColleagueUuid(UUID colleagueUuid) {

        List<TypedAttribute> profileAttributes = findProfileAttributes(colleagueUuid);

        com.tesco.pma.service.colleague.client.model.Colleague colleague = findColleagueByColleagueUuid(colleagueUuid);

        com.tesco.pma.service.colleague.client.model.Colleague lineManger = findLineManager(colleague);

        return Optional.of(fillProfileResponse(colleague, lineManger, profileAttributes));

    }

    @Override
    @Transactional
    public List<TypedAttribute> updateProfileAttributes(UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
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
    public List<TypedAttribute> createProfileAttributes(UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
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
    public List<TypedAttribute> deleteProfileAttributes(UUID colleagueUuid, List<TypedAttribute> profileAttributes) {
        List<TypedAttribute> results = new ArrayList<>();
        profileAttributes.forEach(profileAttribute -> {
            if (1 == profileAttributeDAO.delete(profileAttribute)) {
                results.add(profileAttribute);
            } else {
                throw notFound(COLLEAGUE_UUID_PARAMETER_NAME, profileAttribute.getColleagueUuid());
            }

        });
        return results;
    }

    private AggregatedColleague fillProfileResponse(final com.tesco.pma.service.colleague.client.model.Colleague colleague,
                                                    final com.tesco.pma.service.colleague.client.model.Colleague lineManager,
                                                    final List<TypedAttribute> profileAttributes) {

        AggregatedColleague aggregatedColleagueResponse = new AggregatedColleague();

        aggregatedColleagueResponse.setColleague(convertToColleagueResponse(colleague));

        aggregatedColleagueResponse.setLineManager(convertToColleagueResponse(lineManager));

        aggregatedColleagueResponse.setProfileAttributes(profileAttributes);

        return aggregatedColleagueResponse;
    }

    private Colleague convertToColleagueResponse(com.tesco.pma.service.colleague.client.model.Colleague colleague) {
        Colleague colleagueResponse = new Colleague();

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

    private Profile convertToProfileResponse(com.tesco.pma.service.colleague.client.model.Profile profile) {
        return Optional.ofNullable(profile)
                .map(input -> {
                    var profileResponse = new Profile();
                    BeanUtils.copyProperties(input, profileResponse);
                    return profileResponse;
                }).orElse(null);
    }

    private Contact convertToContactResponse(com.tesco.pma.service.colleague.client.model.Contact contact) {
        return Optional.ofNullable(contact)
                .map(input -> {
                    var contactResponse = new Contact();
                    BeanUtils.copyProperties(input, contactResponse);

                    if (Objects.nonNull(input.getAddresses())) {
                        var addressListResponse = new AddressList();
                        BeanUtils.copyProperties(input.getAddresses(), addressListResponse);
                        contactResponse.setAddresses(addressListResponse);
                    }

                    return contactResponse;
                }).orElse(null);
    }

    private ServiceDates convertToServiceDatesResponse(com.tesco.pma.service.colleague.client.model.service.ServiceDates serviceDates) {
        return Optional.ofNullable(serviceDates)
                .map(input -> {
                    var serviceDatesResponse = new ServiceDates();
                    BeanUtils.copyProperties(input, serviceDatesResponse);
                    return serviceDatesResponse;
                }).orElse(null);

    }

    private List<WorkRelationship> convertToWorkRelationshipsResponse(
            List<com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship> workRelationships) {
        if (Objects.isNull(workRelationships)) {
            return null;
        }

        Optional<com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship> optionalWorkRelationship =
                workRelationships.stream()
                        .filter(IS_WORK_RELATIONSHIP_ACTIVE)
                        .findFirst();

        return optionalWorkRelationship
                .map(input -> {
                    var workRelationshipResponse = new WorkRelationship();
                    workRelationshipResponse.setEmploymentType(input.getEmploymentType());
                    workRelationshipResponse.setWorkSchedule(input.getWorkSchedule()); // Time Type
                    workRelationshipResponse.setEmploymentType(input.getEmploymentType());
                    workRelationshipResponse.setIsManager(input.getIsManager());

                    // Job Title
                    var jobResponse = Optional.ofNullable(input.getJob()).map(job -> {
                        var result = new Job();
                        BeanUtils.copyProperties(job, result);
                        return result;
                    }).orElse(null);
                    workRelationshipResponse.setJob(jobResponse);

                    // Function
                    var departmentResponse = Optional.ofNullable(input.getDepartment()).map(department -> {
                        var result = new Department();
                        BeanUtils.copyProperties(department, result);
                        return result;
                    }).orElse(null);
                    workRelationshipResponse.setDepartment(departmentResponse);

                    return workRelationshipResponse;
                })
                .stream()
                .collect(Collectors.toList());

    }

    private com.tesco.pma.service.colleague.client.model.Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        return colleagueApiService.findColleagueByUuid(colleagueUuid);
    }

    private com.tesco.pma.service.colleague.client.model.Colleague findLineManager(
            com.tesco.pma.service.colleague.client.model.Colleague colleague) {
        if (Objects.isNull(colleague)) {
            return null;
        }

        Optional<UUID> managerUUID = colleague.getWorkRelationships().stream()
                .filter(IS_WORK_RELATIONSHIP_ACTIVE)
                .findFirst()
                .map(com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship::getManagerUUID);

        return managerUUID.map(colleagueApiService::findColleagueByUuid).orElse(null);

    }

    private List<TypedAttribute> findProfileAttributes(UUID colleagueUuid) {
        return profileAttributeDAO.get(colleagueUuid);
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(), messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
