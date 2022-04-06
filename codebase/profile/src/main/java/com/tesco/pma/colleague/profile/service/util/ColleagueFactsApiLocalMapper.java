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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        wr.setPrimaryEntity(oc.getPrimaryEntity());
        wr.setSalaryFrequency(oc.getSalaryFrequency());
        wr.setIsManager(oc.isManager());
        wr.setEmploymentType(oc.getEmploymentType());
        wr.setDepartment(getDepartment(oc));
        wr.setJob(getJob(oc));
        wr.setManagerUUID(oc.getManagerUuid());
        wr.setLocationUUID(oc.getLocationId());
        if (oc.getWorkLevel() != null) {
            wr.setWorkLevel(WorkLevel.getByCode(oc.getWorkLevel().getCode()));
        }
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
            var businessType = ocDp.getBusinessType();
            if (businessType != null) {
                dp.setBusinessType(businessType.getName());
            }
            return dp;
        }
        return null;
    }

    public ColleagueEntity colleagueFactsApiToLocal(Colleague source) {
        var destination = new ColleagueEntity();

        destination.setUuid(source.getColleagueUUID());

        final var profile = source.getProfile();
        if (Objects.nonNull(profile)) {
            BeanUtils.copyProperties(profile, destination);
        }

        final var contact = source.getContact();
        if (Objects.nonNull(contact)) {
            destination.setEmail(contact.getEmail());
        }

        var country = new ColleagueEntity.Country();
        country.setCode(source.getCountryCode());
        country.setName(UNDEFINED_VALUE);
        destination.setCountry(country);

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
            var optionalWorkRelationship = source.getWorkRelationships().stream().findFirst();

            if (optionalWorkRelationship.isPresent()) {
                var workRelationship = optionalWorkRelationship.get();

                destination.setPrimaryEntity(workRelationship.getPrimaryEntity());
                destination.setSalaryFrequency(workRelationship.getSalaryFrequency());
                destination.setEmploymentType(workRelationship.getEmploymentType());

                destination.setManagerUuid(workRelationship.getManagerUUID());
                destination.setManager(BooleanUtils.toBoolean(workRelationship.getIsManager()));

                if (workRelationship.getWorkLevel() != null) {
                    var workLevel = new ColleagueEntity.WorkLevel();
                    workLevel.setCode(workRelationship.getWorkLevel().name());
                    workLevel.setName("Work Level #");
                    destination.setWorkLevel(workLevel);
                }

                var workRelationshipDepartment = workRelationship.getDepartment();
                if (workRelationshipDepartment != null) {
                    var department = mapDepartment(workRelationshipDepartment);
                    destination.setDepartment(department);
                }

                if (workRelationship.getJob() != null) {
                    var job = new ColleagueEntity.Job();
                    job.setId(workRelationship.getJob().getId());
                    job.setCode(workRelationship.getJob().getCode());
                    job.setName(workRelationship.getJob().getName());
                    job.setCostCategory(workRelationship.getJob().getCostCategory());
                    destination.setJob(job);
                }

                destination.setLocationId(workRelationship.getLocationUUID());

                if (workRelationship.getLegalEmployer() != null) {
                    destination.setLegalEntity(workRelationship.getLegalEmployer().getName());
                }
            }
        }

    }

    private ColleagueEntity.Department mapDepartment(Department workRelationshipDepartment) {
        var department = new ColleagueEntity.Department();
        department.setUuid(Optional.ofNullable(profileDAO.findDepartment(workRelationshipDepartment.getId(),
                workRelationshipDepartment.getName(), workRelationshipDepartment.getBusinessType()))
                .map(ColleagueEntity.Department::getUuid)
                .orElseGet(UUID::randomUUID));
        department.setId(workRelationshipDepartment.getId());
        department.setName(workRelationshipDepartment.getName());

        if (StringUtils.isNotEmpty(workRelationshipDepartment.getBusinessType())) {
            var businessType = new ColleagueEntity.Department.BusinessType();
            businessType.setUuid(Optional.ofNullable(profileDAO.findBusinessType(workRelationshipDepartment.getBusinessType()))
                    .map(ColleagueEntity.Department.BusinessType::getUuid)
                    .orElseGet(UUID::randomUUID));
            businessType.setName(workRelationshipDepartment.getBusinessType());
            department.setBusinessType(businessType);
        }
        return department;
    }

    public Collection<String> getColleagueFactsAPISupportedAttributes() {
        return Stream.of(
                "profile.firstName",
                "profile.middleName",
                "profile.lastName",

                "contact.email",
                "contact.addresses.countryCode",

                "externalSystems.iam.id",
                "externalSystems.iam.source",

                "serviceDates.hireDate",
                "serviceDates.leavingDate",

                "workRelationships.primaryEntity",
                "workRelationships.salaryFrequency",
                "workRelationships.employmentType",
                "workRelationships.managerUUID",
                "workRelationships.isManager",
                "workRelationships.workLevel",
                "workRelationships.department.id",
                "workRelationships.department.name",
                "workRelationships.department.businessType",
                "workRelationships.job.id",
                "workRelationships.job.code",
                "workRelationships.job.name",
                "workRelationships.job.costCategory",
                "workRelationships.locationUUID",
                "workRelationships.legalEmployer.name"
        ).collect(Collectors.toList());
    }

}
