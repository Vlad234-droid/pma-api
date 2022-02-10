package com.tesco.pma.colleague.security.rolefetch;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.LocalTestConfig;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.security.UserRoleNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class RoleFetchServiceImplTest {

    @MockBean
    private UserManagementService mockUserManagementService;

    @MockBean
    private RolesMapper mockRolesMapper;

    @MockBean
    private ProfileService mockProfileService;

    @SpyBean
    private RoleFetchServiceImpl roleFetchService;

    private static final UUID COLLEAGUE_UUID = UUID.randomUUID();

    private static final Account ACCOUNT = account();

    private static final String ROLE_PREFIX = "ROLE_";

    @Test
    void findRolesForColleagueShouldReturnOneRole() {
        var colleagueEntity = colleagueEntity(COLLEAGUE_UUID);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(1, roles.size());

        var expectedRoles = expectedRoles(UserRoleNames.COLLEAGUE);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithIsManagerTrueShouldReturnTwoRoles() {
        var colleagueEntity = colleagueEntity(COLLEAGUE_UUID);
        colleagueEntity.setManager(true);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(2, roles.size());

        var expectedRoles = expectedRoles(UserRoleNames.COLLEAGUE, UserRoleNames.LINE_MANAGER);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithWorkLevelWL3ShouldReturnTwoRoles() {
        var colleagueEntity = colleagueEntityWithWorkLevel("WL3");

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(2, roles.size());

        var expectedRoles = expectedRoles(UserRoleNames.COLLEAGUE, UserRoleNames.EXECUTIVE);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithWorkLevelWL1ShouldReturnOneRole() {
        var colleagueEntity = colleagueEntityWithWorkLevel("WL1");

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(1, roles.size());

        var expectedRoles = expectedRoles(UserRoleNames.COLLEAGUE);
        assertIterableEquals(expectedRoles, roles);

    }

    private static Account account() {
        var account = new Account();
        account.setStatus(AccountStatus.ENABLED);
        return account;
    }

    private ColleagueEntity colleagueEntity(UUID uuid) {
        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setUuid(uuid);
        return colleagueEntity;
    }

    private ColleagueEntity colleagueEntityWithWorkLevel(String workLevelCode) {
        var colleagueEntity = new ColleagueEntity();
        var workLevel = new ColleagueEntity.WorkLevel();
        workLevel.setCode(workLevelCode);
        colleagueEntity.setWorkLevel(workLevel);
        return colleagueEntity;
    }

    private List<String> expectedRoles(String... names) {
        return Stream.of(names).map(name -> ROLE_PREFIX + name).collect(toList());
    }

}