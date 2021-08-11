package com.tesco.pma.profile.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Profile model.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {

    /**
     * Identifier.
     */
    UUID uuid;

    String title;

    String firstName;

    String middleName;

    String lastName;

    String gender;

}
