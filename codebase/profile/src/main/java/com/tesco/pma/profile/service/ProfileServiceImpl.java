package com.tesco.pma.profile.service;

import com.tesco.pma.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.Profile;
import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileAttributeDAO profileDAO;
    private final ColleagueApiService colleagueApiService;

    private static final Predicate<WorkRelationship> IS_WORK_RELATIONSHIP_ACTIVE = workRelationship ->
            workRelationship.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE);

    @Override
    public Optional<Profile> findProfileByColleagueUuid(UUID colleagueUuid) {

        List<ProfileAttribute> profileAttributes = findProfileAttributes(colleagueUuid);

        Colleague colleague = findColleague(colleagueUuid);

        Colleague lineManger = findLineManager(colleague);

        return Optional.of(fillProfile(colleague, lineManger, profileAttributes));

    }

    private Profile fillProfile(final Colleague colleague, final Colleague lineManger,
                                final List<ProfileAttribute> profileAttributes) {

        Profile profile = new Profile();

        // Personal information
        final var colleagueProfile = colleague.getProfile();
        if (Objects.nonNull(colleagueProfile)) {
            BeanUtils.copyProperties(colleagueProfile, profile);
        }

        // Contact
        final var contact = colleague.getContact();
        if (Objects.nonNull(contact)) {
            profile.setEmailAddress(contact.getEmail());
            profile.setMobilePhone(contact.getWorkPhoneNumber());
        }

        // Professional information
        final var serviceDates = colleague.getServiceDates();
        if (Objects.nonNull(serviceDates)) {
            profile.setHireDate(serviceDates.getHireDate());
        }

        // Location

        // Profile Attributes
        if (!profileAttributes.isEmpty()) {
            BeanWrapper targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(profile);
            profileAttributes.forEach(profileAttribute ->
                    {
                        String propertyName = profileAttribute.getName();
                        targetBeanWrapper.setPropertyValue(propertyName, profileAttribute.getValue());
                    }
            );
        }

        return profile;
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
        return profileDAO.get(colleagueUuid);
    }


}
