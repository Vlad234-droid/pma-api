package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private LocalDate dateOfBirth;

}
