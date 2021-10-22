package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.UUID;

@Data
public class Colleague {
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
        private String id;
        private String name;
        private String businessType;
    }
}
