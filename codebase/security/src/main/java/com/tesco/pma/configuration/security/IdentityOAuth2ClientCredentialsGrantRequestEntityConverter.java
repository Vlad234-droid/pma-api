package com.tesco.pma.configuration.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;

import java.util.List;

/**
 * Identity specific Converter that converts the provided OAuth2ClientCredentialsGrantRequest to a RequestEntity representation
 * of an OAuth 2.0 Access Token Request for the Client Credentials Grant.
 *
 * <p>Modifies 'Accept' header according to Identity spec.
 *
 * <p>'confidence_level' attribute not implemented.
 * @see <a href="https://github.dev.global.tesco.org/Customer-API-Platform/identityservice/wiki/Endpoint:-V4-Issue-Token">Identity Endpoint:-V4-Issue-Token</a>
 */
public class IdentityOAuth2ClientCredentialsGrantRequestEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {
    public static final MediaType IDENTITY_ISSUE_TOKEN_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.tesco.identity.tokenresponse+json");

    @Override
    public RequestEntity<?> convert(OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {
        //add 'confidence_level' if needed
        RequestEntity<?> requestEntity = super.convert(clientCredentialsGrantRequest);
        final var httpHeaders = HttpHeaders.writableHttpHeaders(requestEntity.getHeaders());
        httpHeaders.setAccept(List.of(IDENTITY_ISSUE_TOKEN_MEDIA_TYPE));

        return new RequestEntity<>(requestEntity.getBody(), httpHeaders, requestEntity.getMethod(), requestEntity.getUrl());
    }
}
