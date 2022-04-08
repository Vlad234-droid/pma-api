package com.tesco.pma.bpm.camunda.configuration;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class PmaAuthenticationProvider implements AuthenticationProvider {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String CAMUNDA_GROUP_ID_PREFIX = "tesco-";

    @Override
    public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return AuthenticationResult.unsuccessful();
        }

        String name = principal.getName();
        if (name == null || name.isEmpty()) {
            return AuthenticationResult.unsuccessful();
        }

        AuthenticationResult authenticationResult = new AuthenticationResult(name, true);

        if (principal instanceof BearerTokenAuthentication) {
            List<String> groupIds = new ArrayList<>();
            List<String> tenantIds = List.of();
            final Collection<GrantedAuthority> authorities = new LinkedHashSet<>(((Authentication) principal).getAuthorities());
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().startsWith(ROLE_PREFIX)) {
                    groupIds.add(mappingAuthorityToGroupId(authority.getAuthority()));
                }
            }
            authenticationResult.setGroups(groupIds);
            authenticationResult.setTenants(tenantIds);
        }

        return authenticationResult;
    }

    private String mappingAuthorityToGroupId(String authority) {
        String role = authority.replace(ROLE_PREFIX, "");
        String[] camelCaseWords = role.split("(?=[A-Z])");
        return CAMUNDA_GROUP_ID_PREFIX + Arrays.stream(camelCaseWords)
                .map(String::toLowerCase)
                .collect(Collectors.joining("-"));
    }

    @Override
    public void augmentResponseByAuthenticationChallenge(HttpServletResponse response, ProcessEngine engine) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + engine.getName() + "\"");
    }

}
