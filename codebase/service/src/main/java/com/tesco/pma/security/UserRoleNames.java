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
    public static final String SUBSIDIARY_MANAGER = "SubsidiaryManager";
    public static final String VIEWER = "Viewer";

    public static final List<String> ALL = List.of(ADMIN, SUBSIDIARY_MANAGER, VIEWER);

}
