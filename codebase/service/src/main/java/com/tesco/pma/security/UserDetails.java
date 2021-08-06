package com.tesco.pma.security;

import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

/**
 * Wrapper for {@link User} used in authorization process.
 */
public class UserDetails extends User {
    @Delegate
    private final User user;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final Map<String, Collection<UUID>> subsidiaryPermissionsByRole = buildSubsidiaryPermissionsByRole();

    public UserDetails(@NonNull User user) {
        this.user = user;
    }

    /**
     * Check if user has particular Subsidiary permission.
     *
     * @param subsidiaryUuid uuid of Subsidiary. Can't be null.
     * @param role           user role. Can't be null.
     * @return true - if user has a Subsidiary permission, false otherwise.
     */
    public boolean hasSubsidiaryPermission(@NonNull UUID subsidiaryUuid, @NonNull String role) {
        return getSubsidiaryPermissionsByRole().getOrDefault(role, Collections.emptySet()).contains(subsidiaryUuid);
    }

    private Map<String, Collection<UUID>> buildSubsidiaryPermissionsByRole() {
        return user.getSubsidiaryPermissions().stream()
                .collect(groupingBy(SubsidiaryPermission::getRole,
                        mapping(SubsidiaryPermission::getSubsidiaryUuid, toCollection(HashSet::new))));
    }
}
