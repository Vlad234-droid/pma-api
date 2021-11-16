package com.tesco.pma.colleague.security.rolefetch;

import com.tesco.pma.colleague.security.rolefetch.RoleFetchService;
import lombok.AllArgsConstructor;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RoleFetchFilter extends OncePerRequestFilter {

    private final RoleFetchService roleFetchService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            this.logger.trace("Did not process request since current SecurityContext is null or unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        UUID colleagueUuid;
        try {
            // colleagueUuid = IdentityToken.subject = Authentication.name
            colleagueUuid = UUID.fromString(authentication.getName());
        } catch (Exception e) {
            // in case 'name' not UUID (e.g. anonymous user, etc.)
            return;
        }

        // Try to find roles in account storage
        var roles = roleFetchService.findRolesInAccountStorage(colleagueUuid);

        try {
            var context = SecurityContextHolder.getContext();
            var authenticationResult = merge(authentication, roles);
            context.setAuthentication(authenticationResult);
            SecurityContextHolder.setContext(context);

            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));

            filterChain.doFilter(request, response);

        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
            this.logger.trace("Failed to process authentication request", failed);
        }

    }

    private Authentication merge(Authentication main, Collection<String> roles) {
        Assert.notNull(main, "main can't be null");

        if (main instanceof BearerTokenAuthentication) {
            final Collection<GrantedAuthority> authorities = new LinkedHashSet<>(main.getAuthorities());
            roles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role));
            });

            final var authentication = new BearerTokenAuthentication((OAuth2AuthenticatedPrincipal) main.getPrincipal(),
                    (OAuth2AccessToken) main.getCredentials(), authorities);
            authentication.setDetails(main.getDetails());
            return authentication;
        }
        return main;
    }

}
