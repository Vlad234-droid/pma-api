package com.tesco.pma.colleague.profile.service.rest.model.colleague.profile;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Profile {
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
}
