package com.tesco.pma.configuration;

import com.tesco.pma.configuration.security.IdentityOAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.function.Supplier;

/**
 * Configuration that expose Identity pma client token supplier ({@link ClientSecurityConfiguration#pmaClientTokenSupplier})
 * to be used to authorize calls to different Tesco APIs.
 */
@Configuration
public class ClientSecurityConfiguration {
    /**
     * Constant principal name for Pma client.
     */
    static final String PMA_PRINCIPAL_NAME = "pma";
    /**
     * From "spring.security.client" config.
     */
    static final String IDENTITY_CLIENT_REGISTRATION_ID = "identity";

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService clientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(configurer -> {
                            final var accessTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
                            accessTokenResponseClient.setRequestEntityConverter(
                                    new IdentityOAuth2ClientCredentialsGrantRequestEntityConverter());

                            configurer.accessTokenResponseClient(accessTokenResponseClient);
                        })
                        .build();

        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Provides Identity token for PMA client.
     *
     * @param clientManager client manager
     * @return supplier that provides pma client token
     */
    @Bean
    public Supplier<String> pmaClientTokenSupplier(OAuth2AuthorizedClientManager clientManager) {
        return () -> clientManager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId(IDENTITY_CLIENT_REGISTRATION_ID)
                .principal(PMA_PRINCIPAL_NAME)
                .build())
                .getAccessToken().getTokenValue();
    }
}
