package com.tesco.pma.colleague.api;

import com.tesco.pma.colleague.api.effectivity.Effectivity;
import com.tesco.pma.colleague.api.service.ServiceDates;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class Colleague implements Serializable {

    private static final long serialVersionUID = 1L;

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

}
