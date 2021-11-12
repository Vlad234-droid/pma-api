package com.tesco.pma.configuration.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Extracts the {@link GrantedAuthority}s from 'groups' attribute and map them
 * using {@link Attributes2GrantedAuthoritiesMapper}. Defaults to {@link SimpleAttributes2GrantedAuthoritiesMapper}.
 * Expects 'groups' comma-separated.
 */
public final class GroupsClaimGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String GROUPS_CLAIM_NAME = "groups";

    private Attributes2GrantedAuthoritiesMapper attributes2GrantedAuthoritiesMapper = new SimpleAttributes2GrantedAuthoritiesMapper();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        final var groups = jwt.getClaimAsString(GROUPS_CLAIM_NAME);
        if (StringUtils.hasText(groups)) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(getGrantedAuthorities(groups)));
        }
        return Collections.emptySet();
    }

    /**
     * Sets the {@link Attributes2GrantedAuthoritiesMapper} to use.
     * Defaults to {@link SimpleAttributes2GrantedAuthoritiesMapper}.
     *
     * @param attributes2GrantedAuthoritiesMapper mapper
     */
    public void setAttributes2GrantedAuthoritiesMapper(Attributes2GrantedAuthoritiesMapper attributes2GrantedAuthoritiesMapper) {
        Assert.notNull(attributes2GrantedAuthoritiesMapper, "attributes2GrantedAuthoritiesMapper can't be null");
        this.attributes2GrantedAuthoritiesMapper = attributes2GrantedAuthoritiesMapper;
    }

    private Collection<? extends GrantedAuthority> getGrantedAuthorities(String groups) {
        return attributes2GrantedAuthoritiesMapper.getGrantedAuthorities(Arrays.asList(groups.split(",")));
    }

}
