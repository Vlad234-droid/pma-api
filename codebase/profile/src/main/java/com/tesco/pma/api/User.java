package com.tesco.pma.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

/**
 * User model.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User extends ColleagueProfile {
    private Collection<String> roles = new HashSet<>();
}