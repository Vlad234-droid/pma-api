package com.tesco.pma.configuration.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class GroupsClaimGrantedAuthoritiesConverterTest {
    private GroupsClaimGrantedAuthoritiesConverter instance;

    @BeforeEach
    void setUp() {
        instance = new GroupsClaimGrantedAuthoritiesConverter();
    }

    @Test
    void convertWithoutGroupsClaimThenEmptyAuthorities() {
        final var jwt = Jwt.withTokenValue("token-value")
                .header("header-name", "header-value")
                .claim("claim-name", "claim-value").build();

        assertThat(instance.convert(jwt)).isEmpty();
    }

    @Test
    void convertWithGroupsClaimThenConvertedAuthorities() {
        final var jwt = Jwt.withTokenValue("token-value")
                .header("header-name", "header-value")
                .claim("groups", "GROUP_1,GROUP_2").build();

        assertThat(instance.convert(jwt)).flatExtracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_GROUP_1", "ROLE_GROUP_2");
    }

    @Test
    void convertWithGroupsClaimWithCustomMapperThenConvertedAuthorities() {
        instance.setAttributes2GrantedAuthoritiesMapper(attributes ->
                attributes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableList()));

        final var jwt = Jwt.withTokenValue("token-value")
                .header("header-name", "header-value")
                .claim("groups", "GROUP_1,GROUP_2").build();

        assertThat(instance.convert(jwt)).flatExtracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("GROUP_1", "GROUP_2");
    }
}