package com.tesco.pma.colleague.security.rolefetch;

import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.security.UserRoleNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RoleFetchService}.
 */
@Service
@AllArgsConstructor
@Slf4j
public class RoleFetchServiceImpl implements RoleFetchService {

    private final UserManagementService userManagementService;
    private final RolesMapper rolesMapper;
    private final ProfileService profileService;

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<String> findRolesInAccountStorage(UUID colleagueUuid) {
        var account = userManagementService.findAccountByColleagueUuid(colleagueUuid);
        if (account != null && AccountStatus.ENABLED.equals(account.getStatus())) {
            var roles = account.getRoles();
            var roleIds = roles.stream()
                    .map(role -> rolesMapper.findRoleByCode(role.getCode()))
                    .filter(role -> UserRoleNames.ALL.contains(role.replace(ROLE_PREFIX, "")))
                    .collect(Collectors.toSet());

            // Add default role ids
            roleIds.add(ROLE_PREFIX + UserRoleNames.COLLEAGUE);

            try {
                var colleague = profileService.getColleague(colleagueUuid);
                // Add Line Manager role
                if (colleague.isManager()) {
                    roleIds.add(ROLE_PREFIX + UserRoleNames.LINE_MANAGER);
                }
                // Add Executive role
                if (hasColleagueExecutiveRole(colleague.getWorkLevel())) {
                    roleIds.add(ROLE_PREFIX + UserRoleNames.EXECUTIVE);
                }
            } catch (NotFoundException exception) {
                log.error(LogFormatter.formatMessage(ErrorCodes.COLLEAGUE_NOT_FOUND,
                        String.format("Colleague %s not found", colleagueUuid)));
            }

            return roleIds;
        }
        return Collections.emptySet();
    }

    private boolean hasColleagueExecutiveRole(ColleagueEntity.WorkLevel workLevel) {
        if (workLevel == null) {
            return false;
        }

        var executiveWorkLevels = EnumSet.of(WorkLevel.WL3, WorkLevel.WL4, WorkLevel.WL5);
        var colleagueWorkLevel = WorkLevel.getByCode(workLevel.getCode());

        return executiveWorkLevels.contains(colleagueWorkLevel);
    }

}
