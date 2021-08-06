package com.tesco.pma.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StaticAuthenticationManagerResolverTest {

    @Test
    void resolve() {
        final var mockAuthenticationManager = mock(AuthenticationManager.class);

        final var resolver = new StaticAuthenticationManagerResolver(mockAuthenticationManager);

        assertThat(resolver.resolve(mock(HttpServletRequest.class))).isSameAs(mockAuthenticationManager);
    }
}