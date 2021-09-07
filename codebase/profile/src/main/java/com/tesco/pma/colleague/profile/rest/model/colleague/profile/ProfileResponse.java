package com.tesco.pma.colleague.profile.rest.model.colleague.profile;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileResponse {
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
}
