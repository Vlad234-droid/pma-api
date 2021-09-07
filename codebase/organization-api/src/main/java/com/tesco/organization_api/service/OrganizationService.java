package com.tesco.organization_api.service;

import com.tesco.organization_api.api.ColleagueOrganizationTree;
import com.tesco.pma.service.cep.EventRequest;
import com.tesco.pma.service.colleague.client.model.Colleague;

import java.util.UUID;

public interface OrganizationService {

    ColleagueOrganizationTree getColleagueTree(UUID colleagueUuid, String departmentId);

    void processCepEvent(EventRequest<Colleague> colleagueEventRequest);
}
