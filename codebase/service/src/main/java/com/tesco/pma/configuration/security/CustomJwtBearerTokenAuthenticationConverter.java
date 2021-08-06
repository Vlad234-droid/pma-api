package com.tesco.pma.configuration.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a
 * {@link BearerTokenAuthentication}.
 *
 * <p>Extracts {@link GrantedAuthority}'s from {@link Jwt} using {@link Converter Converter&lt;Jwt, Collection&lt;GrantedAuthority&gt;&gt;}.
 * Defaults to {@link JwtGrantedAuthoritiesConverter}.
 */
public class CustomJwtBearerTokenAuthenticationConverter implements Converter<Jwt, BearerTokenAuthentication> {
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public BearerTokenAuthentication convert(Jwt jwt) {
        var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(),
                jwt.getIssuedAt(), jwt.getExpiresAt());
        Map<String, Object> attributes = jwt.getClaims();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        var principal = new DefaultOAuth2AuthenticatedPrincipal(attributes, authorities);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    /**
     * Sets the {@link Converter Converter&lt;Jwt, Collection&lt;GrantedAuthority&gt;&gt;}
     * to use. Defaults to {@link JwtGrantedAuthoritiesConverter}.
     *
     * @param jwtGrantedAuthoritiesConverter The converter
     */
    public void setJwtGrantedAuthoritiesConverter(Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }
}