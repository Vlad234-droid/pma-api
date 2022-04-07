package com.tesco.pma.colleague.security.rolefetch;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.LocalTestConfig;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.tesco.pma.colleague.security.TestDataUtils.buildAccount;
import static com.tesco.pma.colleague.security.TestDataUtils.buildColleagueEntity;
import static com.tesco.pma.colleague.security.TestDataUtils.buildRoles;
import static com.tesco.pma.colleague.security.TestDataUtils.buildWorkLevel;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static com.tesco.pma.security.UserRoleNames.LINE_MANAGER;
import static com.tesco.pma.security.UserRoleNames.TALENT_ADMIN;
import static com.tesco.pma.security.UserRoleNames.EXECUTIVE;
import static com.tesco.pma.security.UserRoleNames.PEOPLE_TEAM;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.anyString;
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

    private static final Account ACCOUNT = buildAccount();

    private static final String ROLE_PREFIX = "ROLE_";

    @Test
    void findRolesForColleagueShouldReturnEmptySetRoles() {
        var account = buildAccount();
        account.setStatus(AccountStatus.DISABLED);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(account);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(0, roles.size());
    }

    @Test
    void findRolesForColleagueShouldReturnOneRole() {
        var colleagueEntity = buildColleagueEntity(COLLEAGUE_UUID);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(1, roles.size());

        var expectedRoles = expectedRoles(COLLEAGUE);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithIsManagerTrueShouldReturnTwoRoles() {
        var colleagueEntity = buildColleagueEntity(COLLEAGUE_UUID);
        colleagueEntity.setManager(true);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(2, roles.size());

        var expectedRoles = expectedRoles(COLLEAGUE, LINE_MANAGER);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithWorkLevelWL3ShouldReturnTwoRoles() {
        var colleagueEntity = buildColleagueEntity(COLLEAGUE_UUID);
        colleagueEntity.setWorkLevel(buildWorkLevel("WL3"));

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(2, roles.size());

        var expectedRoles = expectedRoles(COLLEAGUE, EXECUTIVE);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithWorkLevelWL1ShouldReturnOneRole() {
        var colleagueEntity = buildColleagueEntity(COLLEAGUE_UUID);
        colleagueEntity.setWorkLevel(buildWorkLevel("WL1"));

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(ACCOUNT);
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);

        assertEquals(1, roles.size());

        var expectedRoles = expectedRoles(COLLEAGUE);
        assertIterableEquals(expectedRoles, roles);

    }

    @Test
    void findRolesForColleagueWithTwoAccountRolesShouldReturnThreeRoles() {
        var account = buildAccount();
        account.setRoles(buildRoles("PeopleTeam", "TalentAdmin"));

        var colleagueEntity = buildColleagueEntity(COLLEAGUE_UUID);

        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(account);
        when(mockRolesMapper.findRoleByCode(anyString()))
                .thenAnswer(
                        (Answer<String>) invocation -> {
                            Object[] args = invocation.getArguments();
                            return ROLE_PREFIX + args[0];
                        });
        when(mockProfileService.getColleague(COLLEAGUE_UUID))
                .thenReturn(colleagueEntity);

        var roles = roleFetchService.findRolesInAccountStorage(COLLEAGUE_UUID);
        var sortedRoles = roles.stream().sorted().collect(toList());

        assertEquals(3, sortedRoles.size());

        var expectedRoles = expectedRoles(COLLEAGUE, PEOPLE_TEAM, TALENT_ADMIN);
        assertIterableEquals(expectedRoles, sortedRoles);

    }


    private List<String> expectedRoles(String... names) {
        return Stream.of(names).map(name -> ROLE_PREFIX + name).collect(toList());
    }

}