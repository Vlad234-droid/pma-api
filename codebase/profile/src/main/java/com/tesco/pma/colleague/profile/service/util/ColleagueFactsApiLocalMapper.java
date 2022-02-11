package com.tesco.pma.colleague.profile.service.util;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.api.service.ServiceDates;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.Job;
import com.tesco.pma.colleague.api.workrelationships.LegalEmployer;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Class for mapping between Colleague Facts API json format and database representation
 */
@Component
@RequiredArgsConstructor
public class ColleagueFactsApiLocalMapper {

    private final ProfileDAO profileDAO;

    private static final String UNDEFINED_VALUE = "N/A";

    private static final Predicate<WorkRelationship>
            IS_WORK_RELATIONSHIP_ACTIVE = workRelationship -> workRelationship.getWorkingStatus().equals(
            com.tesco.pma.colleague.api.workrelationships.WorkRelationship.WorkingStatus.ACTIVE);

    public Colleague localToColleagueFactsApi(ColleagueEntity oc, UUID colleagueUuid, boolean child) {
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

    private ServiceDates getServiceDates(ColleagueEntity oc) {
        var serviceDates = new ServiceDates();
        serviceDates.setHireDate(oc.getHireDate());
        serviceDates.setLeavingDate(oc.getLeavingDate());
        return serviceDates;
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
        wr.setWorkLevel(WorkLevel.getByCode(oc.getWorkLevel().getCode()));
        wr.setPrimaryEntity(oc.getPrimaryEntity());
        wr.setSalaryFrequency(oc.getSalaryFrequency());
        wr.setIsManager(oc.isManager());
        wr.setEmploymentType(oc.getEmploymentType());
        wr.setDepartment(getDepartment(oc));
        wr.setJob(getJob(oc));
        wr.setManagerUUID(oc.getManagerUuid());
        wr.setLocationUUID(oc.getLocationId());
        if (oc.getLegalEntity() != null) {
            var legalEmployer = new LegalEmployer();
            legalEmployer.setName(oc.getLegalEntity());
            wr.setLegalEmployer(legalEmployer);
        }
        if (wr.getManagerUUID() != null) {
            var mng = profileDAO.getColleague(wr.getManagerUUID());
            if (mng != null) {
                wr.setManager(localToColleagueFactsApi(mng, wr.getManagerUUID(), true));
            }
        }
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

    public ColleagueEntity colleagueFactsApiToLocal(Colleague source) {
        ColleagueEntity destination = new ColleagueEntity();

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
                ColleagueEntity.Country country = new ColleagueEntity.Country();
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

    private void mappingWorkRelationshipProperties(Colleague source, ColleagueEntity destination) {
        if (Objects.nonNull(source.getWorkRelationships())) {
            Optional<WorkRelationship> optionalWorkRelationship = source.getWorkRelationships().stream().findFirst();

            if (optionalWorkRelationship.isPresent()) {
                WorkRelationship workRelationship = optionalWorkRelationship.get();

                destination.setPrimaryEntity(workRelationship.getPrimaryEntity());
                destination.setSalaryFrequency(workRelationship.getSalaryFrequency());
                destination.setEmploymentType(workRelationship.getEmploymentType());

                destination.setManagerUuid(workRelationship.getManagerUUID());
                destination.setManager(workRelationship.getIsManager());

                ColleagueEntity.WorkLevel workLevel =
                        new ColleagueEntity.WorkLevel();
                workLevel.setCode(workRelationship.getWorkLevel().name());
                workLevel.setName("Work Level #");
                destination.setWorkLevel(workLevel);

                ColleagueEntity.Department department =
                        new ColleagueEntity.Department();
                BeanUtils.copyProperties(workRelationship.getDepartment(), department);
                destination.setDepartment(department);

                ColleagueEntity.Job job = new ColleagueEntity.Job();
                BeanUtils.copyProperties(workRelationship.getJob(), job);
                destination.setJob(job);

                destination.setLocationId(workRelationship.getLocationUUID());

                if (workRelationship.getLegalEmployer() != null) {
                    destination.setLegalEntity(workRelationship.getLegalEmployer().getName());
                }
            }
        }

    }

}
