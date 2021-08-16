package com.tesco.pma.service.colleague.client.model.workrelationships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.service.colleague.client.model.Colleague.ColleagueType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkRelationship {

    public enum WorkingStatus {
        ACTIVE, INACTIVE, SUSPENDED, INACTIVE_PAID
    }

    public enum Type {
        PRIMARY
    }

    public enum WorkLevel {
        WL1, WL2, WL3, WL4, WL5;
    }

    UUID locationUUID;
    ContractType contractType;
    ColleagueType colleagueType;
    WorkingStatus workingStatus;
    Type type;
    String defaultExpenseAccount;
    String peopleGroup;
    LegalEmployer legalEmployer;
    Department department;
    Grade grade;
    Position position;
    Job job;
    UUID managerUUID;
    String actionCode;
    String actionReasonCode;
    String userStatus;
    String workSchedule;
    String employmentType;
    String salaryFrequency;
    String workingHours;
    String costCenter;
    String assignmentId;
    String primaryEntity;
    Boolean workingInHiredCountry;
    Boolean isManager;
    WorkLevel workLevel;

}
