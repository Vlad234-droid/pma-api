package com.tesco.pma.cep.v2.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.TooManyFields")
public class ColleagueRevision {
    String colleagueUUID;
    String employeeId;
    String countryCode;
    Effectivity effectivity;
    ExternalSystem externalSystems;
    Profile profile;
    Contact contact;
    ServiceDates serviceDates;
    List<WorkRelationship> workRelationships;
    List<NonTerm> nonTerms;
    List<VisaPermit> visaPermits;
    List<Skill> skills;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Effectivity {
        LocalDate from;
        LocalDate to;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExternalSystem {
        String sourceSystem;
        IamSourceSystem iam;
        HcmSourceSystem hcm;

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class IamSourceSystem {
            String id;
            String name;
            String source;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class HcmSourceSystem {
            Long id;
            String name;
            String type;
            String migrationStatus;
        }
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Profile {
        String title;
        String firstName;
        String middleName;
        String lastName;
        String preferredName;
        LocalDate dateOfBirth;
        String gender;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Contact {
        String email;
        String workPhoneNumber;
        String personalMobileNumber;
        List<Address> addresses;

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class Address {
            List<String> lines;
            String countryCode;
            String postcode;
            String city;
            String county;
        }
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ServiceDates {
        LocalDate hireDate;
        LocalDate leavingDate;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkRelationship {
        String locationUUID;
        ContractType contractType;
        String colleagueType;
        String workingStatus;
        String type;
        String defaultExpenseAccount;
        String peopleGroup;
        Long legalEntityId;
        String managerUUID;
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
        String workLevel;
        LegalEmployer legalEmployer;
        Job job;
        Department department;
        Grade grade;
        Position position;

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class ContractType {
            String sourceCode;
            String sourceName;
            LocalDate endDate;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class LegalEmployer {
            Long id;
            String name;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SuppressWarnings("PMD.ShortClassName")
        public static class Job {
            String id;
            String code;
            String name;
            String costCategory;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class Department {
            String id;
            String name;
            String businessType;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class Grade {
            String id;
            String code;
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class Position {
            String id;
            String code;
            String name;
            String teamName;
        }
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class NonTerm {
        LocalDate startDate;
        LocalDate endDate;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VisaPermit {
        String code;
        String name;
        LocalDate expirationDate;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Skill {
        String code;
        String name;
        String type;
    }

}
