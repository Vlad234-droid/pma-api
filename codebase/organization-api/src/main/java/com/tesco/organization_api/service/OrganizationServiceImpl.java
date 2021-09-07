package com.tesco.organization_api.service;

import com.tesco.organization_api.api.ColleagueOrganizationTree;
import com.tesco.organization_api.api.PmaColleague;
import com.tesco.organization_api.dao.OrganizationDAO;
import com.tesco.pma.service.cep.EventRequest;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.model.Colleague;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final ColleagueApiClient client;
    private final OrganizationDAO dao;

    @Override
    public ColleagueOrganizationTree getColleagueTree(UUID colleagueUuid, String departmentId) {
        var colleagueTree = dao.isColleagueSynced(colleagueUuid)
                ? dao.getColleagueTree(colleagueUuid, departmentId)
                : saveColleagueTree(colleagueUuid);

        enrichColleagueData(colleagueTree.getColleague());
        enrichColleagueData(colleagueTree.getManager());
        colleagueTree.getSubordinates().forEach(this::enrichColleagueData);

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
        updateColleagueInfo(payload);
    }

    private ColleagueOrganizationTree saveColleagueTree(UUID colleagueUuid) {
        var colleague = client.findColleagueByColleagueUuid(colleagueUuid);

        var subordinateUuids = client.findColleagueSubordinates(colleagueUuid)
                .stream()
                .map(Colleague::getColleagueUUID)
                .filter(uuid -> !uuid.equals(colleagueUuid))
                .collect(Collectors.toSet());

        var managerUuids = colleague.getWorkRelationships()
                .stream()
                .filter(wr -> wr.getManagerUUID() != null)
                .filter(uuid -> !uuid.equals(colleagueUuid))
                .collect(Collectors.toSet());

        subordinateUuids.forEach(uuid -> dao.saveColleague(uuid, false));
        managerUuids.forEach(uuid -> dao.saveColleague(uuid, false));
        dao.saveColleague(colleagueUuid, true);

        return colleagueOrganizationTree;
    }

    private void processDepartment(Department department) {
        if (!dao.isDepartmentPresent(department.getId())) {
            dao.saveDepartment(department);
        }
    }

    private void updateColleagueInfo(Colleague colleague) {

        colleague.getWorkRelationships()
                .stream()
                .filter(wr -> wr.getManagerUUID() != null)
                .map(wr -> {
                    builder.manager(getColleague(wr.getManagerUUID()));
                });

        var colleagueOrganizationTree = builder.build();
        dao.upsertColleagueOrganizationTree(colleagueOrganizationTree);

    }

    private void enrichColleagueData(PmaColleague colleague) {
        if (colleague == null) {
            return;
        }
        var factApiColleague = client.findColleagueByColleagueUuid(colleague.getUuid());
        colleague.setFirstName(factApiColleague.getProfile().getFirstName());
        // next copy here

    }

//    private PmaColleague buildColleague(Colleague colleague) {
//        return new PmaColleague();

//    }
//    private PmaColleague tryToGetColleagueFromDbOrElseSave(UUID colleagueUuid) {
//        var colleague = dao.getColleague(colleagueUuid);
//        if (colleague != null) {
//            return colleague;
//        }
//
//        var factApiColleague = client.findColleagueByColleagueUuid(colleagueUuid);
//        colleague = buildColleague(factApiColleague);
//        dao.saveColleague(colleague);
//
//        return colleague;
//    }
}
