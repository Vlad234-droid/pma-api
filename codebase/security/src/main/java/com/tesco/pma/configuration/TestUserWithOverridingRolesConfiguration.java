package com.tesco.pma.configuration;

import com.tesco.pma.api.User;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.validation.AllowedValues;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static com.tesco.pma.security.UserRoleNames.LINE_MANAGER;

/**
 * Configures components to have ability to override user roles for user.
 *
 * <p>Needed for testing proposes.
 */
//TODO:: remove when we will have service users on ppe
@Configuration
@ConditionalOnExpression(TestUserWithOverridingRolesConfiguration.CONDITIONAL_ON_EXPRESSION)
public class TestUserWithOverridingRolesConfiguration {
    static final String TESCO_ROLE_OVERRIDES_ENABLE_PROPERTY_NAME = "tesco.application.security.overriding-roles.enabled";
    static final String CONDITIONAL_ON_EXPRESSION = "${" + TESCO_ROLE_OVERRIDES_ENABLE_PROPERTY_NAME + ":false}"
            + " and ${tesco.application.security.enabled:true}";

    @Bean
    Map<UUID, Collection<String>> overridingRolesByColleagueUuid() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    OverridingRolesInterceptor overridingRolesInterceptor() {
        return new OverridingRolesInterceptor(overridingRolesByColleagueUuid());
    }

    @Bean
    WebMvcConfigurer overridingRolesWebMvcConfigurer(OverridingRolesInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }


    /**
     * 'colleagueUuid' - either colleague UUID or service client id.
     * Could be obtained from IdentityToken 'sub' claim.
     */
    @Hidden
    @RestController
    @RequestMapping("/test/colleagues")
    @Validated
    @ConditionalOnExpression(CONDITIONAL_ON_EXPRESSION)
    static class UserWithOverridingRolesEndpoint {
        private final Map<UUID, Collection<String>> overridingRolesByColleagueUuid;

        public UserWithOverridingRolesEndpoint(Map<UUID, Collection<String>> overridingRolesByColleagueUuid) {
            this.overridingRolesByColleagueUuid = overridingRolesByColleagueUuid;
        }

        @PutMapping(path = "/{colleagueUuid}/overriding-roles", consumes = MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void setOverridingRoles(
                @PathVariable UUID colleagueUuid,
                @RequestBody Collection<@NotNull @AllowedValues({LINE_MANAGER, ADMIN, COLLEAGUE}) String> roleNames) {
            overridingRolesByColleagueUuid.put(colleagueUuid, roleNames);
        }

        @DeleteMapping(path = "/{colleagueUuid}/overriding-roles")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void removeOverridingRoles(@PathVariable UUID colleagueUuid) {
            overridingRolesByColleagueUuid.remove(colleagueUuid);
        }

        @GetMapping(path = "/{colleagueUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
        public User getUser(@PathVariable UUID colleagueUuid) {
            final var roles = overridingRolesByColleagueUuid.get(colleagueUuid);
            if (roles != null) {
                return createUser(colleagueUuid, roles);
            } else {
                throw new NotFoundException(ErrorCodes.USER_NOT_FOUND.getCode(),
                        "User with overriding roles not found for colleagueUuid: " + colleagueUuid);
            }
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public Collection<User> getUsers() {
            return overridingRolesByColleagueUuid.entrySet().stream()
                    .map(entry -> createUser(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        private User createUser(UUID colleagueUuid, Collection<String> roles) {
            final var user = new User();
            final var colleague = new Colleague();
            colleague.setColleagueUUID(colleagueUuid);
            user.setColleague(colleague);
            user.setRoles(roles);
            return user;
        }
    }

    static class OverridingRolesInterceptor implements HandlerInterceptor {
        private static final String ROLE_PREFIX = "ROLE_";
        private static final Set<String> ALL_ROLES_WITH_PREFIX = UserRoleNames.ALL.stream()
                .map(ROLE_PREFIX::concat)
                .collect(Collectors.toSet());

        private final Map<UUID, Collection<String>> roleOverridesMap;

        public OverridingRolesInterceptor(Map<UUID, Collection<String>> roleOverridesMap) {
            this.roleOverridesMap = roleOverridesMap;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth instanceof BearerTokenAuthentication) {
                final var roles = roleOverridesMap.get(UUID.fromString(auth.getName()));
                if (roles != null) {
                    Collection<GrantedAuthority> authorities = new HashSet<>(auth.getAuthorities());

                    authorities.removeIf(grantedAuthority -> ALL_ROLES_WITH_PREFIX.contains(grantedAuthority.getAuthority()));

                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));

                    final var authentication = new BearerTokenAuthentication((OAuth2AuthenticatedPrincipal) auth.getPrincipal(),
                            (OAuth2AccessToken) auth.getCredentials(), authorities);
                    authentication.setDetails(auth.getDetails());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            return true;
        }

    }

}
