package com.tesco.pma.colleague.profile.service.util;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.api.service.ServiceDates;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.Job;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utility class for mapping between Colleague Facts API json format and database representation
 */
@SuppressWarnings("PMD.UseUtilityClass")
@Component
@RequiredArgsConstructor
public class ColleagueFactsApiLocalMapper {

    private final ConfigEntryDAO configEntryDAO;

    private static final String UNDEFINED_VALUE = "N/A";

    private static final Predicate<WorkRelationship>
            IS_WORK_RELATIONSHIP_ACTIVE = workRelationship -> workRelationship.getWorkingStatus().equals(
            com.tesco.pma.colleague.api.workrelationships.WorkRelationship.WorkingStatus.ACTIVE);

    public Colleague localToColleagueFactsApi(com.tesco.pma.organisation.api.Colleague oc, UUID colleagueUuid,
                                                     boolean child) {
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
                wr.setManager(localToColleagueFactsApi(mng, wr.getManagerUUID(), true));
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

    public com.tesco.pma.organisation.api.Colleague colleagueFactsApiToLocal(Colleague source) {
        com.tesco.pma.organisation.api.Colleague destination = new com.tesco.pma.organisation.api.Colleague();

        destination.setUuid(source.getColleagueUUID());

        final var profile = source.getProfile();
        if (Objects.nonNull(profile)) {
            BeanUtils.copyProperties(profile, destination);
        }

        final var contact = source.getContact();
        if (Objects.nonNull(contact)) {
            destination.setEmail(contact.getEmail());

            final var addresses = contact.getAddresses();
            if (Objects.nonNull(addresses)) {
                com.tesco.pma.organisation.api.Colleague.Country country = new com.tesco.pma.organisation.api.Colleague.Country();
                country.setCode(addresses.getCountryCode());
                country.setName(UNDEFINED_VALUE);
                destination.setCountry(country);
            }
        }

        final var externalSystems = source.getExternalSystems();
        if (Objects.nonNull(externalSystems) && Objects.nonNull(externalSystems.getIam())) {
            destination.setIamId(externalSystems.getIam().getId());
            destination.setIamSource(externalSystems.getIam().getSource());
        }

        final var serviceDates = source.getServiceDates();
        if (Objects.nonNull(serviceDates)) {
            destination.setHireDate(serviceDates.getHireDate());
            destination.setLeavingDate(serviceDates.getLeavingDate());
        }

        mappingWorkRelationshipProperties(source, destination);

        return destination;
    }

    private void mappingWorkRelationshipProperties(Colleague source,
                                                          com.tesco.pma.organisation.api.Colleague destination) {
        if (Objects.nonNull(source.getWorkRelationships())) {
            Optional<WorkRelationship> optionalWorkRelationship = source.getWorkRelationships().stream().findFirst();

            if (optionalWorkRelationship.isPresent()) {
                WorkRelationship workRelationship = optionalWorkRelationship.get();

                destination.setPrimaryEntity(workRelationship.getPrimaryEntity());
                destination.setSalaryFrequency(workRelationship.getSalaryFrequency());
                destination.setEmploymentType(workRelationship.getEmploymentType());

                destination.setManagerUuid(workRelationship.getManagerUUID());
                destination.setManager(workRelationship.getIsManager());

                com.tesco.pma.organisation.api.Colleague.WorkLevel workLevel =
                        new com.tesco.pma.organisation.api.Colleague.WorkLevel();
                workLevel.setCode(workRelationship.getWorkLevel().name());
                workLevel.setName("Work Level #");
                destination.setWorkLevel(workLevel);

                com.tesco.pma.organisation.api.Colleague.Department department =
                        new com.tesco.pma.organisation.api.Colleague.Department();
                BeanUtils.copyProperties(workRelationship.getDepartment(), department);
                destination.setDepartment(department);

                com.tesco.pma.organisation.api.Colleague.Job job = new com.tesco.pma.organisation.api.Colleague.Job();
                BeanUtils.copyProperties(workRelationship.getJob(), job);
                destination.setJob(job);
            }
        }

    }

}
