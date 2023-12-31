package com.tesco.pma.colleague.profile.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ColleagueEntity { //NOPMD
    private UUID uuid;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private WorkLevel workLevel;
    private String primaryEntity;
    private Country country;
    private Department department;
    private String salaryFrequency;
    private Job job;
    private String iamSource;
    private String iamId;
    private UUID managerUuid;
    private String employmentType;
    private LocalDate hireDate;
    private LocalDate leavingDate;
    private boolean manager;
    private String locationId;
    private String legalEntity;

    @Data
    public static class Job { //NOPMD
        private String id;
        private String name;
        private String code;
        private String costCategory;
    }

    @Data
    public static class Country {
        private String name;
        private String code;
    }

    @Data
    public static class WorkLevel {
        private String name;
        private String code;
    }

    @Data
    public static class Department {
        private UUID uuid;
        private String id;
        private String name;
        private BusinessType businessType;

        @Data
        public static class BusinessType {
            private UUID uuid;
            private String name;
        }
    }

    @Override
    public String toString() {
        return "ColleagueEntity("
                + "uuid=" + uuid
                + ", firstName='*****'"
                + ", middleName='*****'"
                + ", lastName='*****'"
                + ", email='*****'"
                + ", workLevel=" + workLevel
                + ", primaryEntity='" + primaryEntity + '\''
                + ", country=" + country
                + ", department=" + department
                + ", salaryFrequency='" + salaryFrequency + '\''
                + ", job=" + job
                + ", iamSource='" + iamSource + '\''
                + ", iamId='*****'"
                + ", managerUuid=" + managerUuid
                + ", employmentType='" + employmentType + '\''
                + ", hireDate=" + hireDate
                + ", leavingDate=" + leavingDate
                + ", manager=" + manager
                + ", locationId='" + locationId + '\''
                + ", legalEntity='" + legalEntity + '\''
                + ')';
    }
}
