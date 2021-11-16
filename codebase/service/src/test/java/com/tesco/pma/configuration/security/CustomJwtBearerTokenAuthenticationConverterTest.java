package com.tesco.pma.configuration.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class CustomJwtBearerTokenAuthenticationConverterTest {

    private CustomJwtBearerTokenAuthenticationConverter instance;

    @BeforeEach
    void setUp() {
        instance = new CustomJwtBearerTokenAuthenticationConverter();
    }

    @Test
    void convertWhenJwtThenBearerTokenAuthentication() {
        Jwt jwt = jwtBuilderWithDefault()
                .claim("claim", "claim_value")
                .build();

        AbstractAuthenticationToken res = this.instance.convert(jwt);

        assertThat(res).asInstanceOf(type(BearerTokenAuthentication.class))
                .returns("token-value", from(auth -> auth.getToken().getTokenValue()))
                .satisfies(auth -> assertThat(auth.getTokenAttributes()).containsExactly(entry("claim", "claim_value")))
                .satisfies(auth -> assertThat(auth.getAuthorities()).isEmpty());
    }

    @Test
    void convertWhenJwtWithScopeAttributeThenBearerTokenAuthentication() {
        Jwt jwt = jwtBuilderWithDefault()
                .claim("scope", "message:read message:write")
                .build();

        AbstractAuthenticationToken res = this.instance.convert(jwt);

        assertThat(res).asInstanceOf(type(BearerTokenAuthentication.class))
                .satisfies(auth -> assertThat(auth.getAuthorities()).containsExactly(new SimpleGrantedAuthority("SCOPE_message:read"),
                                                                                     new SimpleGrantedAuthority("SCOPE_message:write")));
    }

    @Test
    void convertWhenJwtWithScpAttributeThenBearerTokenAuthentication() {
        Jwt jwt = jwtBuilderWithDefault()
                .claim("scp", Arrays.asList("message:read", "message:write"))
                .build();

        AbstractAuthenticationToken res = this.instance.convert(jwt);

        assertThat(res).asInstanceOf(type(BearerTokenAuthentication.class))
                .satisfies(auth -> assertThat(auth.getAuthorities()).flatExtracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_message:read", "SCOPE_message:write"));
    }

    @Test
    void convertWhenJwtWithCustomAuthoritiesConverterThenBearerTokenAuthentication() {
        instance.setJwtGrantedAuthoritiesConverter(jwt -> AuthorityUtils.createAuthorityList(
                "EXTRACTED_AUTHORITY_1", "EXTRACTED_AUTHORITY_2"));
        Jwt jwt = jwtBuilderWithDefault()
                .claim("scp", Arrays.asList("message:read", "message:write"))
                .build();

        AbstractAuthenticationToken res = this.instance.convert(jwt);

        assertThat(res).asInstanceOf(type(BearerTokenAuthentication.class))
                .satisfies(auth -> assertThat(auth.getAuthorities()).flatExtracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("EXTRACTED_AUTHORITY_1", "EXTRACTED_AUTHORITY_2"));
    }

    private Jwt.Builder jwtBuilderWithDefault() {
        return Jwt.withTokenValue("token-value")
                .header("header", "value");
    }

}