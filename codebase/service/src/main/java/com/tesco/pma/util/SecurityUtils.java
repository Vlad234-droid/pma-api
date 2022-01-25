package com.tesco.pma.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@UtilityClass
public class SecurityUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Check if user has role
     * @param authentication currently authenticated principal, or an authentication request token
     * @param role is role to check
     * @return true - if user has specified role
     */
    public boolean hasAuthority(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals(ROLE_PREFIX + role));
    }

    /**
     * Return colleague identifier by authentication
     * @param authentication currently authenticated principal, or an authentication request token
     * @return UUID of colleague
     */
    public UUID getColleagueUuid(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}