package com.tesco.pma.configuration.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdditionalBearerTokenAuthenticationFilterTest {
    public static final String TOKEN_VALUE = "token";
    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    @Mock
    private BearerTokenResolver bearerTokenResolver;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;
    private TestingAuthenticationToken currentAuthentication;

    @BeforeEach
    void setUp() {
        currentAuthentication = new TestingAuthenticationToken("test-user", "test-creds");
        currentAuthentication.setAuthenticated(true);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(currentAuthentication);
        SecurityContextHolder.setContext(securityContext);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }


    @Test
    void doFilterWhenBearerTokenPresentThenAuthenticates() throws ServletException, IOException {
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManager));
        filter.doFilter(request, response, filterChain);
        ArgumentCaptor<BearerTokenAuthenticationToken> captor = ArgumentCaptor
                .forClass(BearerTokenAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(TOKEN_VALUE);
    }

    @Test
    void doFilterWhenUsingAuthenticationManagerResolverThenAuthenticates() throws Exception {
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManagerResolver));
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        when(authenticationManagerResolver.resolve(any())).thenReturn(authenticationManager);
        filter.doFilter(request, response, filterChain);
        ArgumentCaptor<BearerTokenAuthenticationToken> captor = ArgumentCaptor
                .forClass(BearerTokenAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(TOKEN_VALUE);
    }

    @Test
    void doFilterWhenNoBearerTokenPresentThenDoesNotAuthenticate() throws ServletException, IOException {
        when(bearerTokenResolver.resolve(request)).thenReturn(null);
        dontAuthenticate();
    }

    @Test
    void doFilterWhenNoCurrentAuthenticationThenDoesNotAuthenticate() throws ServletException, IOException {
        final var emptyContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(emptyContext);
        dontAuthenticate();
    }

    @Test
    void doFilterWhenCurrentAuthenticationNotAuthenticatedThenDoesNotAuthenticate() throws ServletException, IOException {
        final var securityContext = SecurityContextHolder.createEmptyContext();
        final var authentication = new TestingAuthenticationToken("test-user", "test-creds");
        authentication.setAuthenticated(false);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        dontAuthenticate();
    }

    @Test
    void doFilterWhenCurrentAuthenticationAuthenticatedThenAuthenticatesAndMergeDefault() throws ServletException, IOException {
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManagerResolver));
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        when(authenticationManagerResolver.resolve(any())).thenReturn(authenticationManager);
        filter.doFilter(request, response, filterChain);
        ArgumentCaptor<BearerTokenAuthenticationToken> captor = ArgumentCaptor
                .forClass(BearerTokenAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(TOKEN_VALUE);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(currentAuthentication);
    }

    @Test
    void doFilterWhenCurrentAuthenticationAuthenticatedThenAuthenticatesAndMergeCustom() throws ServletException, IOException {
        final var mockSecondary = mock(Authentication.class);
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManagerResolver));
        filter.setAuthenticationMerger((main, secondary) -> secondary);
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        when(authenticationManagerResolver.resolve(any())).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(mockSecondary);
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(mockSecondary);
    }

    @Test
    void doFilterWhenMalformedBearerTokenThenPropagatesError() throws ServletException, IOException {
        BearerTokenError error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST,
                "description", "uri");
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(error);
        when(bearerTokenResolver.resolve(request)).thenThrow(exception);
        dontAuthenticate();
        verify(authenticationEntryPoint).commence(request, response, exception);
    }

    @Test
    void doFilterWhenAuthenticationFailsWithDefaultHandlerThenPropagatesError()
            throws ServletException, IOException {
        BearerTokenError error = new BearerTokenError(BearerTokenErrorCodes.INVALID_TOKEN, HttpStatus.UNAUTHORIZED,
                "description", "uri");
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(error);
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        when(authenticationManager.authenticate(any(BearerTokenAuthenticationToken.class))).thenThrow(exception);
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManager));
        filter.doFilter(request, response, filterChain);
        verify(authenticationEntryPoint).commence(request, response, exception);
    }

    @Test
    void doFilterWhenAuthenticationFailsWithCustomHandlerThenPropagatesError()
            throws ServletException, IOException {
        BearerTokenError error = new BearerTokenError(BearerTokenErrorCodes.INVALID_TOKEN, HttpStatus.UNAUTHORIZED,
                "description", "uri");
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(error);
        when(bearerTokenResolver.resolve(request)).thenReturn(TOKEN_VALUE);
        when(authenticationManager.authenticate(any(BearerTokenAuthenticationToken.class))).thenThrow(exception);
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManager));
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filter.doFilter(request, response, filterChain);
        verify(authenticationFailureHandler).onAuthenticationFailure(request, response, exception);
    }

    @Test
    void setAuthenticationEntryPointWhenNullThenThrowsException() {
        AdditionalBearerTokenAuthenticationFilter filter = new AdditionalBearerTokenAuthenticationFilter(authenticationManager);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> filter.setAuthenticationEntryPoint(null))
                .withMessageContaining("authenticationEntryPoint cannot be null");
    }

    @Test
    void setBearerTokenResolverWhenNullThenThrowsException() {
        AdditionalBearerTokenAuthenticationFilter filter = new AdditionalBearerTokenAuthenticationFilter(authenticationManager);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> filter.setBearerTokenResolver(null))
                .withMessageContaining("bearerTokenResolver cannot be null");
    }

    @Test
    void setAuthenticationMergerWhenNullThenThrowsException() {
        AdditionalBearerTokenAuthenticationFilter filter = new AdditionalBearerTokenAuthenticationFilter(authenticationManager);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> filter.setAuthenticationMerger(null))
                .withMessageContaining("authenticationMerger cannot be null");
    }

    @Test
    void constructorWhenNullAuthenticationManagerThenThrowsException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AdditionalBearerTokenAuthenticationFilter((AuthenticationManager) null))
                .withMessageContaining("authenticationManager cannot be null");
    }

    @Test
    void constructorWhenNullAuthenticationManagerResolverThenThrowsException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AdditionalBearerTokenAuthenticationFilter((AuthenticationManagerResolver<HttpServletRequest>) null))
                .withMessageContaining("authenticationManagerResolver cannot be null");
    }

    private AdditionalBearerTokenAuthenticationFilter addMocks(AdditionalBearerTokenAuthenticationFilter filter) {
        filter.setAuthenticationEntryPoint(authenticationEntryPoint);
        filter.setBearerTokenResolver(bearerTokenResolver);
        return filter;
    }

    private void dontAuthenticate() throws ServletException, IOException {
        AdditionalBearerTokenAuthenticationFilter filter = addMocks(
                new AdditionalBearerTokenAuthenticationFilter(authenticationManager));
        filter.doFilter(request, response, filterChain);
        verifyNoMoreInteractions(authenticationManager);
    }
}