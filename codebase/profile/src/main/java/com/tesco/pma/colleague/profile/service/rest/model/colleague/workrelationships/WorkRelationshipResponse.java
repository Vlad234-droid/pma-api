package com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkRelationshipResponse {
    DepartmentResponse department;
    JobResponse job;
    String workSchedule;
    String employmentType;
    Boolean isManager;
}
