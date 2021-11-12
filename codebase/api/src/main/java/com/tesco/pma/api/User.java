package com.tesco.pma.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * User model.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User {
    private UUID colleagueUuid;

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private String email;

    private Collection<String> roles = new HashSet<>();

    public User(UUID colleagueUuid) {
        this.colleagueUuid = colleagueUuid;
    }
}