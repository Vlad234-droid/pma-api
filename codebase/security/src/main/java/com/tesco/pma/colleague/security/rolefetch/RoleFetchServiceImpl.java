package com.tesco.pma.colleague.security.rolefetch;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.security.UserRoleNames;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RoleFetchService}.
 */
@Service
@AllArgsConstructor
public class RoleFetchServiceImpl implements RoleFetchService {

    private final ProfileService profileService;
    private final UserManagementService userManagementService;
    private final RolesMapper rolesMapper;

    @Override
    public Collection<String> findRolesInAccountStorage(UUID colleagueUuid) {
        ColleagueEntity colleague = profileService.getColleague(colleagueUuid);
        if (colleague != null && colleague.getIamId() != null) {
            Account account = userManagementService.findAccountByIamId(colleague.getIamId());
            if (account != null) {
                Collection<Role> roles = account.getRoles();
                return roles.stream()
                        .map(role -> rolesMapper.findRoleByCode(role.getCode()))
                        .filter(role -> UserRoleNames.ALL.contains(role.replaceAll("ROLE_", "")))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptySet();
    }

}
