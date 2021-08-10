package com.tesco.pma.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile model.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

}
