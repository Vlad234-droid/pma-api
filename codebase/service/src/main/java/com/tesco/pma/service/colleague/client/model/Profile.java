package com.tesco.pma.service.colleague.client.model;

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
