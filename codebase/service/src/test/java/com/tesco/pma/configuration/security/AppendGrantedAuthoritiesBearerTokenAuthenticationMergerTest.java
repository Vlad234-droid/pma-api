package com.tesco.pma.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;

class AppendGrantedAuthoritiesBearerTokenAuthenticationMergerTest {
    private final AppendGrantedAuthoritiesBearerTokenAuthenticationMerger instance
            = new AppendGrantedAuthoritiesBearerTokenAuthenticationMerger();

    @Test
    void mergeMainIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> instance.merge(null, mock(Authentication.class)))
                .withMessage("main can't be null");
    }

    @Test
    void mergeSecondaryIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> instance.merge(mock(Authentication.class), null))
                .withMessage("secondary can't be null");
    }

    @Test
    void mergeMainNotABearerTokenAuthenticationPassThroughMain() {
        final var main = mock(Authentication.class);
        assertThat(instance.merge(main, mock(Authentication.class))).isSameAs(main);
    }

    @Test
    void mergeMainBearerTokenAuthenticationSecondaryWithAuthorities() {
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
                Instant.now(), Instant.now().plusSeconds(3600));
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put(OAuth2IntrospectionClaimNames.SUBJECT, "sub");
        attributesMap.put(OAuth2IntrospectionClaimNames.CLIENT_ID, "client_id");
        attributesMap.put(OAuth2IntrospectionClaimNames.USERNAME, "username");
        DefaultOAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributesMap, null);
        final var authorityFromMain = "AUTHORITY_FROM_MAIN";
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(authorityFromMain);

        final var main = new BearerTokenAuthentication(principal, token, authorities);
        final var authorityFromSecondary = "AUTHORITY_FROM_SECONDARY";
        final var secondary = new TestingAuthenticationToken(null, null, authorityFromSecondary);

        final var res = instance.merge(main, secondary);

        assertThat(res).usingRecursiveComparison().ignoringFields("authorities").isEqualTo(main);
        assertThat(res.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(authorityFromMain, authorityFromSecondary);
    }

    @Test
    void mergeAddsUserAuthorityWithSecondaryTokenAttributes() {
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
                Instant.now(), Instant.now().plusSeconds(3600));
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put(OAuth2IntrospectionClaimNames.SUBJECT, "sub");
        attributesMap.put(OAuth2IntrospectionClaimNames.CLIENT_ID, "client_id");
        attributesMap.put(OAuth2IntrospectionClaimNames.USERNAME, "username");
        DefaultOAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributesMap, null);
        final var authorityFromMain = "AUTHORITY_FROM_MAIN";
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(authorityFromMain);
        final Map<String, Object> secondaryTokenAttributes = Map.of("attr_name", "attr_value");

        final var main = new BearerTokenAuthentication(principal, token, authorities);
        final var authorityFromSecondary = "AUTHORITY_FROM_SECONDARY";
        final var secondary = new BearerTokenAuthentication(
                new DefaultOAuth2AuthenticatedPrincipal(secondaryTokenAttributes, Collections.emptyList()),
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, random(36), Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS)),
                AuthorityUtils.createAuthorityList(authorityFromSecondary));

        final var res = instance.merge(main, secondary);

        assertThat(res)
                .satisfies(authentication -> assertThat(authentication.getAuthorities())
                        .filteredOn(OAuth2UserAuthority.class::isInstance)
                        .hasSize(1)
                        .first(type(OAuth2UserAuthority.class))
                        .extracting(OAuth2UserAuthority::getAttributes).isEqualTo(secondaryTokenAttributes))
                .satisfies(authentication -> assertThat(authentication.getAuthorities())
                        .map(GrantedAuthority::getAuthority)
                        .contains(authorityFromMain, authorityFromSecondary))
                .usingRecursiveComparison().ignoringFields("authorities").isEqualTo(main);
    }
}