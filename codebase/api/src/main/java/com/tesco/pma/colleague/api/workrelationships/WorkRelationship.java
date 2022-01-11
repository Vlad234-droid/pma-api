package com.tesco.pma.colleague.api.workrelationships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Colleague.ColleagueType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.TooManyFields")
public class WorkRelationship implements Serializable {

    private static final long serialVersionUID = 8091059063936530669L;

    public enum WorkingStatus {
        ACTIVE, INACTIVE, SUSPENDED, INACTIVE_PAID
    }

    public enum Type {
        PRIMARY
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
    UUID managerUUID;
    Colleague manager;
}
