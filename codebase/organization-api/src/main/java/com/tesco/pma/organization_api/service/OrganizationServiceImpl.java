package com.tesco.pma.organization_api.service;

import com.tesco.pma.organization_api.api.ColleagueStructure;
import com.tesco.pma.organization_api.api.PmaColleague;
import com.tesco.pma.organization_api.dao.OrganizationDAO;
import com.tesco.pma.service.cep.EventRequest;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.workrelationships.Department;
import com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final ColleagueApiClient client;
    private final OrganizationDAO dao;

    @Override
    public ColleagueStructure getColleagueTree(UUID colleagueUuid, String departmentId) {
        var colleagueTree = dao.isColleagueSynced(colleagueUuid)
                ? dao.getColleagueTree(colleagueUuid, departmentId)
                : saveColleagueTree(colleagueUuid, departmentId);

        enrichColleagueData(colleagueTree.getColleague(), departmentId);
        enrichColleagueData(colleagueTree.getManager(), departmentId);
        colleagueTree.getSubordinates().forEach(colleague -> enrichColleagueData(colleague, departmentId));

        return colleagueTree;
    }

    @Override
    public void processCepEvent(EventRequest<Colleague> colleagueEventRequest) {
        var payload = colleagueEventRequest.getPayload();
        var colleagueUuid = payload.getColleagueUUID();
        if (!dao.isColleagueSynced(colleagueUuid)) {
            //Colleague was not synced. Skip update.
            return;
        }

        //department part
        processDepartments(payload.getWorkRelationships().stream()
                .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                .map(WorkRelationship::getDepartment).collect(Collectors.toSet()));

        //colleague part
        var managerUuids = payload.getWorkRelationships()
                .stream()
                .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                .filter(wr -> wr.getManagerUUID() != null)
                .collect(Collectors.toMap(WorkRelationship::getManagerUUID, wr -> wr.getDepartment().getId()));
        managerUuids.entrySet().stream()
                .filter(Predicate.not(es -> dao.isWorkRelationshipExists(colleagueUuid, es.getKey(), es.getValue())))
                .forEach(es -> {
                    dao.saveColleague(es.getKey(), false);
                    dao.saveWorkRelationship(colleagueUuid, es.getKey(), es.getValue());
                });
    }

    private ColleagueStructure saveColleagueTree(UUID colleagueUuid, String departmentId) {
        var colleague = client.findColleagueByColleagueUuid(colleagueUuid);
        var subordinates = client.findColleagueSubordinates(colleagueUuid);

        //department part
        var departments = colleague.getWorkRelationships().stream()
                .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                .map(WorkRelationship::getDepartment).collect(Collectors.toSet());
        departments.addAll(subordinates.stream().flatMap(c -> c.getWorkRelationships().stream())
                .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                .map(WorkRelationship::getDepartment).collect(Collectors.toSet()));
        processDepartments(departments);

        //colleague part
        var subordinateUuids = getSubordinatesWithDepartment(subordinates, colleagueUuid);

        var managerUuids = colleague.getWorkRelationships()
                .stream()
                .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                .filter(wr -> wr.getManagerUUID() != null)
                .collect(Collectors.toMap(WorkRelationship::getManagerUUID, wr -> wr.getDepartment().getId()));

        subordinateUuids.keySet().forEach(uuid -> dao.saveColleague(uuid, false));
        managerUuids.keySet().forEach(uuid -> dao.saveColleague(uuid, false));
        dao.saveColleague(colleagueUuid, true);

        subordinateUuids.forEach((key, value) -> value.forEach(depId -> dao.saveWorkRelationship(key, colleagueUuid, depId)));
        managerUuids.forEach((key, value) -> dao.saveWorkRelationship(colleagueUuid, key, value));

        return populateOrgTree(colleagueUuid, departmentId, subordinateUuids, managerUuids);
    }

    private ColleagueStructure populateOrgTree(UUID colleagueUuid, String departmentId, Map<UUID, Set<String>> subordinateUuids, Map<UUID, String> managerUuids) {
        var colleagueOrganizationTree = new ColleagueStructure();
        colleagueOrganizationTree.setColleague(new PmaColleague(colleagueUuid));
        colleagueOrganizationTree.setManager(managerUuids.entrySet().stream()
                .filter(es -> es.getValue().equals(departmentId))
                .findFirst().map(Map.Entry::getKey).map(PmaColleague::new).orElse(null));
        colleagueOrganizationTree.setSubordinates(subordinateUuids.entrySet().stream()
                .filter(es -> es.getValue().contains(departmentId))
                .map(Map.Entry::getKey).map(PmaColleague::new).collect(Collectors.toSet()));
        return colleagueOrganizationTree;
    }

    private Map<UUID, Set<String>> getSubordinatesWithDepartment(List<Colleague> subordinates, UUID colleagueUuid) {
        var result = new HashMap<UUID, Set<String>>();
        for (var subordinate : subordinates) {
            if (subordinate.getColleagueUUID().equals(colleagueUuid)) {
                continue;
            }
            result.put(subordinate.getColleagueUUID(), subordinate.getWorkRelationships()
                    .stream()
                    .filter(wr -> wr.getWorkingStatus().equals(WorkRelationship.WorkingStatus.ACTIVE))
                    .filter(wr -> wr.getManagerUUID().equals(colleagueUuid))
                    .map(WorkRelationship::getDepartment)
                    .map(Department::getId)
                    .collect(Collectors.toSet()));
        }
        return result;
    }

    private void processDepartments(Collection<Department> departments) {
        departments.forEach(this::processDepartment);
    }

    private void processDepartment(Department department) {
        if (!dao.isDepartmentPresent(department.getId())) {
            dao.saveDepartment(department);
        }
    }

    private void enrichColleagueData(PmaColleague colleague, String departmentId) {
        if (colleague == null) {
            return;
        }
        var factApiColleague = client.findColleagueByColleagueUuid(colleague.getUuid());

        colleague.setEmployeeId(factApiColleague.getEmployeeId());

        var profile = factApiColleague.getProfile();
        if (profile != null) {
            colleague.setFirstName(profile.getFirstName());
            colleague.setLastName(profile.getLastName());
            colleague.setMiddleName(profile.getMiddleName());
            colleague.setGender(profile.getGender());
            colleague.setTitle(profile.getTitle());
        }

        var contact = factApiColleague.getContact();
        if (contact != null) {
            colleague.setEmail(contact.getEmail());
            colleague.setWorkPhoneNumber(contact.getWorkPhoneNumber());
        }

        factApiColleague.getWorkRelationships().stream()
                .filter(wr -> wr.getDepartment().getId().equals(departmentId))
                .findFirst()
                .ifPresent(wr -> {
                    colleague.setDepartment(wr.getDepartment());
                    colleague.setGrade(wr.getGrade());
                    colleague.setJob(wr.getJob());
                    colleague.setPosition(wr.getPosition());
                });
    }
}
