package com.tesco.pma.configuration.security;

import com.tesco.pma.security.UserRoleNames;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static com.tesco.pma.security.UserRoleNames.LINE_MANAGER;
import static com.tesco.pma.security.UserRoleNames.PEOPLE_TEAM;
import static com.tesco.pma.security.UserRoleNames.PROCESS_MANAGER;
import static com.tesco.pma.security.UserRoleNames.TALENT_ADMIN;

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
 * <p>{@link #isColleague()}
 *
 * <p>{@link #isLineManager()}
 *
 * <p>{@link #isPeopleTeam()}
 *
 * <p>{@link #isTalentAdmin()}
 *
 * <p>{@link #isProcessManager()}
 *
 * <p>Example: @PreAuthorize("isAdmin() or isColleague() or isLineManager()")
 */
@Slf4j
public class PmaMethodSecurityExpressionOperations implements MethodSecurityExpressionOperations {

    @Delegate
    @NonNull
    private final MethodSecurityExpressionOperations delegate;

    public PmaMethodSecurityExpressionOperations(@NonNull MethodSecurityExpressionOperations delegate) {
        this.delegate = delegate;
        log.info("MethodSecurityExpressionOperations delegate: {}", delegate);
    }

    /**
     * Check if user has {@link UserRoleNames#ADMIN} role.
     *
     * @return true - if user is a Admin, false otherwise.
     */
    public boolean isAdmin() {
        return hasRole(ADMIN);
    }

    /**
     * Check if user has {@link UserRoleNames#COLLEAGUE} role.
     *
     * @return true - if user is a Colleague, false otherwise.
     */
    public boolean isColleague() {
        return hasRole(COLLEAGUE);
    }

    public boolean isCurrentUser(UUID userId){
        if(userId==null){
            return false;
        }

        var currentUserId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return userId.equals(currentUserId);
    }

    /**
     * Check if user has {@link UserRoleNames#LINE_MANAGER} role.
     *
     * @return true - if user is a Line Manager, false otherwise.
     */
    public boolean isLineManager() {
        return hasRole(LINE_MANAGER);
    }

    /**
     * Check if user has {@link UserRoleNames#PEOPLE_TEAM} role.
     *
     * @return true - if user is a People Team, false otherwise.
     */
    public boolean isPeopleTeam() {
        return hasRole(PEOPLE_TEAM);
    }

    /**
     * Check if user has {@link UserRoleNames#TALENT_ADMIN} role.
     *
     * @return true - if user is a Talent Admin, false otherwise.
     */
    public boolean isTalentAdmin() {
        return hasRole(TALENT_ADMIN);
    }

    /**
     * Check if user has {@link UserRoleNames#PROCESS_MANAGER} role.
     *
     * @return true - if user is a Process Manager, false otherwise.
     */
    public boolean isProcessManager() {
        return hasRole(PROCESS_MANAGER);
    }

}
