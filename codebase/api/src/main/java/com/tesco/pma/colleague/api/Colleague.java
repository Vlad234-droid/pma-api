package com.tesco.pma.colleague.api;

import com.tesco.pma.colleague.api.effectivity.Effectivity;
import com.tesco.pma.colleague.api.service.ServiceDates;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tesco.pma.api.Identified;
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
