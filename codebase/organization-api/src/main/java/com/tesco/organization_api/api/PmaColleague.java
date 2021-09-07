package com.tesco.organization_api.api;

import lombok.Data;

import java.util.UUID;

@Data
public class PmaColleague {
    private UUID uuid;
    private String employeeId;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private String gender;
    private String email;
    private String workPhoneNumber;

    private Object department;
    private Object job;
    private Object grade;
    private Object position;
}
