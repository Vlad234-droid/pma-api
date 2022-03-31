package com.tesco.pma.configuration.security;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.security.UserRoleNames;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.UUID;

import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.ALL;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static com.tesco.pma.security.UserRoleNames.EXECUTIVE;
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
 * <p>{@link #isExecutive()}
 *
 * <p>Example: @PreAuthorize("isAdmin() or isColleague() or isLineManager()")
 */
@Slf4j
public class PmaMethodSecurityExpressionOperations implements MethodSecurityExpressionOperations {

    @Delegate
    @NonNull
    private final MethodSecurityExpressionOperations delegate;

    @NonNull
    private final ProfileService profileService;

    public PmaMethodSecurityExpressionOperations(@NonNull MethodSecurityExpressionOperations delegate,
                                                 @NonNull ProfileService profileService) {
        this.delegate = delegate;
        this.profileService = profileService;
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

    public boolean isCurrentUser(UUID userId) {
        if (userId == null) {
            return false;
        }

        var currentUserId = getCurrentUserId();
        return userId.equals(currentUserId);
    }

    /**
     * Check if user is manager of colleague
     *
     * @param colleagueUuid an identifier of colleague
     * @param depth         a level of colleague's tree
     * @return true - if user is manager of colleague.
     */
    public boolean isManagerOf(UUID colleagueUuid, Integer depth) {
        if (colleagueUuid == null || depth == null) {
            return false;
        }

        var currentUserUuid = getCurrentUserId();
        var currentUser = profileService.getColleague(currentUserUuid);
        if (Boolean.FALSE.equals(currentUser.isManager())) {
            return false;
        }
        var managerUuid = colleagueUuid;
        for (var level = 1; level <= depth; level++) {
            managerUuid = profileService.getColleague(managerUuid).getManagerUuid();
            if (managerUuid == null) {
                return false;
            }

            if (currentUserUuid.equals(managerUuid)) {
                return true;
            }
        }
        return false;
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

    /**
     * Check if user has {@link UserRoleNames#EXECUTIVE} role.
     *
     * @return true - if user is a Executive, false otherwise.
     */
    public boolean isExecutive() {
        return hasRole(EXECUTIVE);
    }

    /**
     * Check if user has any role.
     *
     * @return true - if user has an any role, false otherwise.
     */
    public boolean hasAnyRole() {
        return hasAnyRole(ALL.toArray(new String[0]));
    }

    /**
     * Check if user has {@link UserRoleNames#COLLEAGUE} role and at least one of {@code workLevelCodes}
     *
     * @return true - if user is a Colleague of work level, false otherwise.
     */
    public boolean hasColleagueWorkLevel(String... workLevelCodes) {
        var currentUserId = getCurrentUserId();
        var colleague = profileService.getColleague(currentUserId);
        var workLevelCodesList = Arrays.asList(workLevelCodes);

        if (!CollectionUtils.isEmpty(workLevelCodesList) && hasRole(COLLEAGUE)) {
            return workLevelCodesList.stream().anyMatch(wl -> wl.equalsIgnoreCase(colleague.getWorkLevel().getCode()));
        }
        return false;
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
