package com.tesco.pma.configuration.security;

import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copy of {@link org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter}.
 * Differences:
 *
 * <p>1. Proceed only if current Authentication is authenticated.
 *
 * <p>2. in case of authentication success  - merge existing and additional auth using {@link AuthenticationMerger}.
 * Default merger returns current Authentication.
 */
//TODO:: Implement check Identity user is same as OneLogin user. // NOSONAR
public class AdditionalBearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    private AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

    private AuthenticationFailureHandler authenticationFailureHandler =
            (request, response, exception) -> this.authenticationEntryPoint.commence(request, response, exception);

    private AuthenticationMerger authenticationMerger = (main, secondary) -> main;

    /**
     * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s)
     *
     * @param authenticationManagerResolver
     */
    public AdditionalBearerTokenAuthenticationFilter(
            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
        Assert.notNull(authenticationManagerResolver, "authenticationManagerResolver cannot be null");
        this.authenticationManagerResolver = authenticationManagerResolver;
    }

    /**
     * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s)
     *
     * @param authenticationManager
     */
    public AdditionalBearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.authenticationManagerResolver = request -> authenticationManager;
    }

    /**
     * Extract any
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
     * Token</a> from the request and attempt an authentication.
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) //NOPMD
            throws ServletException, IOException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            this.logger.trace("Did not process request since current SecurityContext is null or unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        try {
            token = this.bearerTokenResolver.resolve(request);
        } catch (OAuth2AuthenticationException invalid) {
            this.logger.trace("Sending to authentication entry point since failed to resolve bearer token", invalid);
            this.authenticationEntryPoint.commence(request, response, invalid);
            return;
        }
        if (token == null) {
            this.logger.trace("Did not process request since did not find bearer token");
            filterChain.doFilter(request, response);
            return;
        }
        var authenticationRequest = new BearerTokenAuthenticationToken(token);
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        try {
            var authenticationManager = this.authenticationManagerResolver.resolve(request);
            var authenticationResult = authenticationManager.authenticate(authenticationRequest);
            this.logger.debug(LogMessage.format("Additional authentication is %s", authenticationResult));

            var context = SecurityContextHolder.getContext();
            authenticationResult = authenticationMerger.merge(context.getAuthentication(), authenticationResult);
            context.setAuthentication(authenticationResult);
            SecurityContextHolder.setContext(context);

            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
            filterChain.doFilter(request, response);
        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
            this.logger.trace("Failed to process authentication request", failed);
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    /**
     * Set the {@link BearerTokenResolver} to use. Defaults to
     * {@link DefaultBearerTokenResolver}.
     *
     * @param bearerTokenResolver the {@code BearerTokenResolver} to use
     */
    public void setBearerTokenResolver(BearerTokenResolver bearerTokenResolver) {
        Assert.notNull(bearerTokenResolver, "bearerTokenResolver cannot be null");
        this.bearerTokenResolver = bearerTokenResolver;
    }

    /**
     * Set the {@link AuthenticationEntryPoint} to use. Defaults to
     * {@link BearerTokenAuthenticationEntryPoint}.
     *
     * @param authenticationEntryPoint the {@code AuthenticationEntryPoint} to use
     */
    public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} to use. Default implementation invokes
     * {@link AuthenticationEntryPoint}.
     *
     * @param authenticationFailureHandler the {@code AuthenticationFailureHandler} to use
     * @since 5.2
     */
    public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    /**
     * Set the {@link AuthenticationMerger} to use. Default implementation returns main authentication.
     *
     * @param authenticationMerger the {@code AuthenticationMerger} to use
     */
    public void setAuthenticationMerger(AuthenticationMerger authenticationMerger) {
        Assert.notNull(authenticationMerger, "authenticationMerger cannot be null");
        this.authenticationMerger = authenticationMerger;
    }
}
