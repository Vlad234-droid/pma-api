package com.tesco.pma.service.colleague.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tesco.pma.api.Identified;
import com.tesco.pma.service.colleague.client.model.effectivity.Effectivity;
import com.tesco.pma.service.colleague.client.model.service.ServiceDates;
import com.tesco.pma.service.colleague.client.model.workrelationships.WorkRelationship;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Colleague implements Identified<UUID> {

    public enum ColleagueType {
        EMPLOYEE,
        EXTERNAL,
        CONTRACTOR
    }

    private UUID colleagueUUID;
    private String employeeId;
    private String countryCode;
    private Effectivity effectivity;
    private ExternalSystems externalSystems;
    private Profile profile;
    private Contact contact;
    private ServiceDates serviceDates;
    private List<WorkRelationship> workRelationships;

    @Override
    @JsonIgnore
    public UUID getId() {
        return colleagueUUID;
    }
}
