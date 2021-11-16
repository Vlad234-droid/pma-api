package com.tesco.pma.configuration.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Extracts granted authorities from 'secondary' authentication and add them to 'main' (make a copy).
 * If secondary is a subclass of {@link AbstractOAuth2TokenAuthenticationToken} then adds additional {@link OAuth2UserAuthority}
 * with token attributes.
 *
 * <p>Expects 'main' be an instance of {@link BearerTokenAuthentication} otherwise passthrough main.
 */
public class AppendGrantedAuthoritiesBearerTokenAuthenticationMerger implements AuthenticationMerger {

    @Override
    public Authentication merge(Authentication main, Authentication secondary) {
        Assert.notNull(main, "main can't be null");
        Assert.notNull(secondary, "secondary can't be null");

        if (main instanceof BearerTokenAuthentication) {
            final Collection<GrantedAuthority> authorities = new LinkedHashSet<>(main.getAuthorities());
            authorities.addAll(secondary.getAuthorities());

            if (secondary instanceof AbstractOAuth2TokenAuthenticationToken) {
                authorities.add(
                        new OAuth2UserAuthority(((AbstractOAuth2TokenAuthenticationToken<?>) secondary).getTokenAttributes()));
            }

            final var authentication = new BearerTokenAuthentication((OAuth2AuthenticatedPrincipal) main.getPrincipal(),
                    (OAuth2AccessToken) main.getCredentials(), authorities);
            authentication.setDetails(main.getDetails());
            return authentication;
        }
        return main;
    }
}
