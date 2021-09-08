package com.tesco.pma.colleague.profile.service.rest.model.colleague;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.contact.Contact;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.profile.Profile;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.service.ServiceDates;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships.WorkRelationship;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Colleague {
    private UUID colleagueUUID;
    private String employeeId;
    private String countryCode;
    private Profile profile;
    private Contact contact;
    private ServiceDates serviceDates;
    private List<WorkRelationship> workRelationships;
}
