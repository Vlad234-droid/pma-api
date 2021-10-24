package com.tesco.pma.colleague.api.workrelationships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.api.Colleague.ColleagueType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.TooManyFields")
public class WorkRelationship {

    public enum WorkingStatus {
        ACTIVE, INACTIVE, SUSPENDED, INACTIVE_PAID
    }

    public enum Type {
        PRIMARY
    }

    public enum WorkLevel {
        WL1, WL2, WL3, WL4, WL5;

        public static WorkLevel getByCode(String code) {
            for (WorkLevel wl : values()) {
                return wl.name().equalsIgnoreCase(code) ? wl : null;
            }
            return null;
        }
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
