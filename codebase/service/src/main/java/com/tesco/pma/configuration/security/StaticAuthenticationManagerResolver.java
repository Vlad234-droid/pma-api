package com.tesco.pma.configuration.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Always resovles to a provided {@link AuthenticationManager}.
 */
public class StaticAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    private final AuthenticationManager authenticationManager;

    public StaticAuthenticationManagerResolver(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager can't be null");
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest context) {
        return this.authenticationManager;
    }
}
