package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Profile implements Serializable {

    private static final long serialVersionUID = -11139684158912855L;

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private LocalDate dateOfBirth;

    @Override
    public String toString() {
        return "Profile{"
                + "title='" + title + '\''
                + ", firstName='*****'"
                + ", middleName='*****'"
                + ", lastName='*****'"
                + ", gender='" + gender + '\''
                + ", dateOfBirth=" + dateOfBirth
                + '}';
    }
}
