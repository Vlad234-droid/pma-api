package com.tesco.pma.configuration.security;

import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Identity specific token introspector.
 * Must be used with Identity v4 Introspection endpoint.
 *
 * <p>Delegates to {@link NimbusOpaqueTokenIntrospector}. Sets 'Accept' request header
 * with 'application/vnd.tesco.identity.v4claims+json' according to Identity spec.
 *
 * @see <a href="https://github.dev.global.tesco.org/Customer-API-Platform/identityservice/wiki/Endpoint:-V4-Introspection">
 * Identity Endpoint: V4 Introspection</a>
 */
public class IdentityOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    public static final MediaType IDENTITY_INTROSPECT_V4_WITH_CLAIMS_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.tesco.identity.v4claims+json");

    private final NimbusOpaqueTokenIntrospector delegate;

    public IdentityOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret, RestTemplate restTemplate) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clientSecret, "clientSecret cannot be null");
        Assert.notNull(restTemplate, "restTemplate cannot be null");

        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setAccept(List.of(IDENTITY_INTROSPECT_V4_WITH_CLAIMS_MEDIA_TYPE));

            // replace Content-Type response header 'application/vnd.tesco.identity.v4claims+json' with 'application/json'
            // Nimbus expects 'application/json' or throws exception.
            final var response = execution.execute(request, body);
            if (IDENTITY_INTROSPECT_V4_WITH_CLAIMS_MEDIA_TYPE.equals(response.getHeaders().getContentType())) {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }
            return response;
        });

        this.delegate = new NimbusOpaqueTokenIntrospector(introspectionUri, restTemplate);
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return delegate.introspect(token);
    }

}
