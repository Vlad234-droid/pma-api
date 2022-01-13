package com.tesco.pma.configuration.security.testing;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@UtilityClass
public class AuthenticationUtils {

    static Authentication createDefaultUserAuthentication(String defaultUserUuid) {
        Map<String, Object> userAttributes = Collections.singletonMap("name", defaultUserUuid);
        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(userAttributes)),
                userAttributes, "name");

        OAuth2AccessToken credentials = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
                Instant.now(), Instant.now().plus(Duration.ofDays(365)));

        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read");

        final var authentication = new BearerTokenAuthentication(principal, credentials, authorities);
        authentication.setDetails(null);

        return authentication;
    }

}
