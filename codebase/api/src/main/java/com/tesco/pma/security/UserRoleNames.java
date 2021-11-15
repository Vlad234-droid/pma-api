package com.tesco.pma.security;


import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Constants for user roles names.
 * Without spring specific 'ROLE_' prefix.
 * Roles in {@link Authentication#getAuthorities()} must be with 'ROLE_' prefix to use
 * this constants in 'hasRole()'.
 */
@UtilityClass
@SuppressWarnings("PMD.ClassNamingConventions")
public class UserRoleNames {
    public static final String ADMIN = "Admin";
    public static final String COLLEAGUE = "Colleague";
    public static final String LINE_MANAGER = "LineManager";
    public static final String PEOPLE_TEAM = "PeopleTeam";
    public static final String TALENT_ADMIN = "TalentAdmin";
    public static final String PROCESS_MANAGER = "ProcessManager";

    public static final List<String> ALL = List.of(ADMIN, COLLEAGUE, LINE_MANAGER, PEOPLE_TEAM, TALENT_ADMIN, PROCESS_MANAGER);

}
