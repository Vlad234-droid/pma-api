package com.tesco.pma.colleague.profile.service.rest.model.colleague;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.contact.ContactResponse;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.profile.ProfileResponse;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.service.ServiceDatesResponse;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships.WorkRelationshipResponse;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColleagueResponse {
    private UUID colleagueUUID;
    private String employeeId;
    private String countryCode;
    private ProfileResponse profile;
    private ContactResponse contact;
    private ServiceDatesResponse serviceDates;
    private List<WorkRelationshipResponse> workRelationships;
}
