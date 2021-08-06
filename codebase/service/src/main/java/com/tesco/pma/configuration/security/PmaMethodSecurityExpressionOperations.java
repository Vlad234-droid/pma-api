package com.tesco.pma.configuration.security;

import com.tesco.pma.api.User;
import com.tesco.pma.security.UserDetails;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;

import java.util.EnumSet;
import java.util.UUID;

import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.SUBSIDIARY_MANAGER;
import static com.tesco.pma.security.UserRoleNames.VIEWER;

/**
 * Pma specific {@link MethodSecurityExpressionOperations}.
 * Used in
 * {@link org.springframework.security.access.prepost.PreAuthorize}
 * {@link org.springframework.security.access.prepost.PostAuthorize}
 * {@link org.springframework.security.access.prepost.PreFilter}
 * {@link org.springframework.security.access.prepost.PostFilter}
 *
 * <p>PMA specific methods:
 *
 * <p>{@link #isAdmin()}
 *
 * <p>{@link #isViewer()}
 *
 * <p>{@link #isManagerOf(UUID)}
 *
 * <p>Example: @PreAuthorize("isAdmin() or isViewer() or isManagerFor(#subsidiaryUuid)")
 */
public class PmaMethodSecurityExpressionOperations implements MethodSecurityExpressionOperations {
    static final UserDetails NULL_USER_DETAILS = new UserDetails(new User(UUID.fromString("00000000-0000-0000-0000-000000000000")));

    @Delegate
    @NonNull
    private final MethodSecurityExpressionOperations delegate;

    @NonNull
    private final UserService userService;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final UserDetails userDetails = retrieveUserDetails();

    public PmaMethodSecurityExpressionOperations(@NonNull MethodSecurityExpressionOperations delegate, @NonNull UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    /**
     * Check if user has {@link UserRoleNames#ADMIN} role.
     *
     * @return true - if user is a Viewer, false otherwise.
     */
    public boolean isAdmin() {
        return hasRole(ADMIN);
    }

    /**
     * Check if user has {@link UserRoleNames#VIEWER} role.
     *
     * @return true - if user is a Viewer, false otherwise.
     */
    public boolean isViewer() {
        return hasRole(VIEWER);
    }

    /**
     * Check if user has {@link UserRoleNames#SUBSIDIARY_MANAGER} role and is a Manager of a particular Subsidiary.
     *
     * @param subsidiaryUuid uuid of the Subsidiary. Can be null.
     * @return true - if user is a manager, false otherwise. If subsidiaryUuid is null returns false.
     */
    public boolean isManagerOf(UUID subsidiaryUuid) {
        if (subsidiaryUuid == null) {
            return false;
        }
        return hasRole(SUBSIDIARY_MANAGER) && getUserDetails().hasSubsidiaryPermission(subsidiaryUuid, SUBSIDIARY_MANAGER);
    }

    private UserDetails retrieveUserDetails() {
        return userService.findUserByAuthentication(getAuthentication(), EnumSet.of(UserIncludes.SUBSIDIARY_PERMISSIONS))
                .map(UserDetails::new)
                .orElse(NULL_USER_DETAILS);
    }
}
