package com.tesco.pma.profile.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Profile model.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile {

    // Personal information
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;

    // Contact
    private String emailAddress;
    private String mobilePhone;
    private String emergencyContact;
    private String emergencyPhone;

    // Professional information
    LocalDate hireDate;
    String employmentType;
    String jobTitle;
    String function;
    String lineManager;
    String businessUnitBonus;
    String timeType;

    // Location

}
