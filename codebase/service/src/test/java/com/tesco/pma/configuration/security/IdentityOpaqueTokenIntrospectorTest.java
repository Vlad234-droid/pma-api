package com.tesco.pma.configuration.security;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IdentityOpaqueTokenIntrospector}
 */
class IdentityOpaqueTokenIntrospectorTest {

    private static final String CLIENT_ID = "client";

    private static final String CLIENT_SECRET = "secret";

    private static final String ACTIVE_RESPONSE = "{\n"
            + "      \"active\": true,\n"
            + "      \"client_id\": \"l238j323ds-23ij4\",\n"
            + "      \"username\": \"jdoe\",\n"
            + "      \"scope\": \"read write dolphin\",\n"
            + "      \"sub\": \"Z5O3upPC88QrAjx00dis\",\n"
            + "      \"aud\": \"https://protected.example.net/resource\",\n"
            + "      \"iss\": \"https://server.example.com/\",\n"
            + "      \"exp\": 1419356238,\n"
            + "      \"iat\": 1419350238,\n"
            + "      \"extension_field\": \"twenty-seven\"\n"
            + "     }";


    @Test
    void introspectWhenActiveTokenThenOk() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(requiresAuth(CLIENT_ID, CLIENT_SECRET, ACTIVE_RESPONSE));
            String introspectUri = server.url("/introspect").toString();
            OpaqueTokenIntrospector introspectionClient = new IdentityOpaqueTokenIntrospector(introspectUri, CLIENT_ID,
                    CLIENT_SECRET, new RestTemplate());
            OAuth2AuthenticatedPrincipal authority = introspectionClient.introspect("token");

            assertThat(authority.getAttributes())
                    .isNotNull()
                    .containsEntry(OAuth2IntrospectionClaimNames.ACTIVE, true)
                    .containsEntry(OAuth2IntrospectionClaimNames.AUDIENCE,
                            Arrays.asList("https://protected.example.net/resource"))
                    .containsEntry(OAuth2IntrospectionClaimNames.CLIENT_ID, "l238j323ds-23ij4")
                    .containsEntry(OAuth2IntrospectionClaimNames.EXPIRES_AT, Instant.ofEpochSecond(1419356238)) //NOPMD
                    .containsEntry(OAuth2IntrospectionClaimNames.ISSUER, new URL("https://server.example.com/"))
                    .containsEntry(OAuth2IntrospectionClaimNames.SCOPE, Arrays.asList("read", "write", "dolphin"))
                    .containsEntry(OAuth2IntrospectionClaimNames.SUBJECT, "Z5O3upPC88QrAjx00dis")
                    .containsEntry(OAuth2IntrospectionClaimNames.USERNAME, "jdoe")
                    .containsEntry("extension_field", "twenty-seven");
        }
    }

    private static Dispatcher requiresAuth(String username, String password, String response) {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                return Optional.ofNullable(authorization)
                        .filter((a) -> isAuthorized(authorization, username, password))
                        .map((a) -> ok(response))
                        .orElse(unauthorized());
            }
        };
    }

    private static boolean isAuthorized(String authorization, String username, String password) {
        String[] values = new String(Base64.getDecoder().decode(authorization.substring(6))).split(":");
        return username.equals(values[0]) && password.equals(values[1]);
    }

    private static MockResponse ok(String response) { //NOPMD
        return new MockResponse().setBody(response)
                .setHeader(HttpHeaders.CONTENT_TYPE, IdentityOpaqueTokenIntrospector.IDENTITY_INTROSPECT_V4_WITH_CLAIMS_MEDIA_TYPE);
    }

    private static MockResponse unauthorized() {
        return new MockResponse().setResponseCode(401);
    }

}
