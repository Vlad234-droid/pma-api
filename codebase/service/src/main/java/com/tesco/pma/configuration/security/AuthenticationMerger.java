package com.tesco.pma.configuration.security;

import org.springframework.security.core.Authentication;

/**
 * Merge two {@link Authentication}'s.
 * First argument must be 'main', second - 'secondary'.
 */
@FunctionalInterface
public interface AuthenticationMerger {
    /**
     * Merge two {@link Authentication}'s.
     * First argument must be 'main', second - 'secondary'.
     *
     * @param main      not null
     * @param secondary not null
     * @return merged {@link Authentication}. not null.
     */
    Authentication merge(Authentication main, Authentication secondary);
}