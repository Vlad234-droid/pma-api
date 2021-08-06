package com.tesco.pma.configuration.security;

import lombok.Data;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;

/**
 * Properties for additional authentication.
 */
@Data
public class AdditionalAuthProperties {
    private static final String DEFAULT_TOKEN_HEADER_NAME = "Authorization-App";

    private String tokenHeaderName = DEFAULT_TOKEN_HEADER_NAME;

    private OAuth2ResourceServerProperties.Jwt jwt = new OAuth2ResourceServerProperties.Jwt();
}