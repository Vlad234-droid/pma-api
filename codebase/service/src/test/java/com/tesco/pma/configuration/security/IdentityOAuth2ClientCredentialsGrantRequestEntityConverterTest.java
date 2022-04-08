package com.tesco.pma.configuration.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class IdentityOAuth2ClientCredentialsGrantRequestEntityConverterTest {
    private static final MediaType IDENTITY_ISSUE_TOKEN_MEDIA_TYPE =
            IdentityOAuth2ClientCredentialsGrantRequestEntityConverter.IDENTITY_ISSUE_TOKEN_MEDIA_TYPE;

    private IdentityOAuth2ClientCredentialsGrantRequestEntityConverter instance;

    @BeforeEach
    void setUp() {
        instance = new IdentityOAuth2ClientCredentialsGrantRequestEntityConverter();
    }

    @Test
    @SuppressWarnings("unchecked")
    void convertSuccess() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("registration-1")
                .clientId("client-1")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read", "write")
                .tokenUri("https://provider.com/oauth2/token")
                .build();

        final var clientCredentialsGrantRequest = new OAuth2ClientCredentialsGrantRequest(clientRegistration);

        RequestEntity<?> requestEntity = instance.convert(clientCredentialsGrantRequest);

        Assertions.assertThat(requestEntity)
                .returns(HttpMethod.POST, from(RequestEntity::getMethod))
                .returns("https://provider.com/oauth2/token", from(entity -> entity.getUrl().toASCIIString()));

        Assertions.assertThat(requestEntity.getHeaders()).asInstanceOf(type(HttpHeaders.class))
                .returns(List.of(IDENTITY_ISSUE_TOKEN_MEDIA_TYPE), HttpHeaders::getAccept)
                .returns(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"),
                        from(HttpHeaders::getContentType));

        Assertions.assertThat((MultiValueMap<String, String>) requestEntity.getBody())
                .containsEntry(OAuth2ParameterNames.GRANT_TYPE, List.of(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
                .containsEntry(OAuth2ParameterNames.SCOPE, List.of("read write"))
                .containsEntry(OAuth2ParameterNames.CLIENT_ID, List.of("client-1"))
                .containsEntry(OAuth2ParameterNames.CLIENT_SECRET, List.of("secret"));
    }

}