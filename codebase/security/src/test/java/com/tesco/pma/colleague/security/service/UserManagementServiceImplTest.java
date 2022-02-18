package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.LocalTestConfig;
import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.colleague.security.TestDataUtils.buildAccount;
import static com.tesco.pma.colleague.security.TestDataUtils.buildAccounts;
import static com.tesco.pma.colleague.security.TestDataUtils.buildCreateAccountRequest;
import static com.tesco.pma.colleague.security.TestDataUtils.buildRoleRequest;
import static com.tesco.pma.colleague.security.TestDataUtils.buildRoles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @MockBean
    private AccountManagementDAO mockAccountManagementDAO;

    @MockBean
    private ProfileService mockProfileService;

    @MockBean
    private NamedMessageSourceAccessor messages;

    @SpyBean
    private UserManagementServiceImpl userManagementService;

    @Test
    void getRoles() {
        when(mockAccountManagementDAO.findRoles())
                .thenReturn(buildRoles(8));

        var roles = userManagementService.getRoles();

        assertEquals(8, roles.size());
    }

    @Test
    void getAllRoles() {
        when(mockAccountManagementDAO.findAllRoles())
                .thenReturn(buildRoles(12));

        var roles = userManagementService.getAllRoles();

        assertEquals(12, roles.size());
    }

    @Test
    void getAccountsSuccessfully() {

        when(mockAccountManagementDAO.get(any(RequestQuery.class)))
                .thenReturn(buildAccounts(3));

        var accounts = userManagementService.getAccounts(1);

        assertEquals(3, accounts.size());

        verify(mockAccountManagementDAO, times(1)).get(any(RequestQuery.class));
    }

    @Test
    void createAccountSuccessfully() {

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(null)
                .thenReturn(buildAccount());
        when(mockAccountManagementDAO.assignRole(any(UUID.class), anyInt()))
                .thenReturn(1);

        var createAccountRequest = buildCreateAccountRequest(3);
        userManagementService.createAccount(createAccountRequest);

        verify(mockAccountManagementDAO, times(2)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(1)).create(anyString(), anyString(),
                any(AccountStatus.class), any(AccountType.class));
        verify(mockAccountManagementDAO, times(1)).assignRole(any(UUID.class), anyInt());
    }

    @Test
    void createAccountWithColleagueUuidSuccessfully() {

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(null)
                .thenReturn(buildAccount());
        when(mockAccountManagementDAO.assignRole(any(UUID.class), anyInt()))
                .thenReturn(1);

        var createAccountRequest = buildCreateAccountRequest(3);
        userManagementService.createAccount(createAccountRequest, UUID.randomUUID());

        verify(mockAccountManagementDAO, times(2)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(1)).create(any(UUID.class), anyString(), anyString(),
                any(AccountStatus.class), any(AccountType.class), any(Instant.class));
        verify(mockAccountManagementDAO, times(1)).assignRole(any(UUID.class), anyInt());
    }

    @Test
    void grantRoleSuccessfully() {
        var account = buildAccount();

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(account);
        when(mockAccountManagementDAO.assignRole(any(UUID.class), anyInt()))
                .thenReturn(1);

        var roleRequest = buildRoleRequest(1);

        userManagementService.grantRole(roleRequest);

        verify(mockAccountManagementDAO, times(1)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(1)).assignRole(any(UUID.class), anyInt());
    }

    @Test
    void grantRoleThrowAlreadyExistsException() {
        var account = buildAccount();

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(account);
        when(mockAccountManagementDAO.assignRole(any(UUID.class), anyInt()))
                .thenThrow(DuplicateKeyException.class);

        var roleRequest = buildRoleRequest(1);

        var exception = assertThrows(AlreadyExistsException.class,
                () -> userManagementService.grantRole(roleRequest));

        assertEquals(ErrorCodes.SECURITY_DUPLICATED_ROLE.name(), exception.getCode());

        verify(mockAccountManagementDAO, times(1)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(1)).assignRole(any(UUID.class), anyInt());
    }

    @Test
    void grantRoleThrowNotFoundException() {

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(null);

        var roleRequest = buildRoleRequest(1);

        var exception = assertThrows(NotFoundException.class,
                () -> userManagementService.grantRole(roleRequest));

        assertEquals(ErrorCodes.SECURITY_ACCOUNT_NOT_FOUND.name(), exception.getCode());

        verify(mockAccountManagementDAO, times(1)).findAccountByName(anyString());
    }

    @Test
    void grantRolesSuccessfully() {
        var account = buildAccount();

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(account);
        when(mockAccountManagementDAO.assignRole(any(UUID.class), anyInt()))
                .thenReturn(1);

        var roleRequest = buildRoleRequest(List.of(1, 2, 3));

        userManagementService.grantRole(roleRequest);

        verify(mockAccountManagementDAO, times(1)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(3)).assignRole(any(UUID.class), anyInt());
    }

    @Test
    void revokeRoleSuccessfully() {
        var account = buildAccount();

        when(mockAccountManagementDAO.findAccountByName(anyString()))
                .thenReturn(account);
        when(mockAccountManagementDAO.removeRole(any(UUID.class), anyInt()))
                .thenReturn(1);

        var roleRequest = buildRoleRequest(1);

        userManagementService.revokeRole(roleRequest);

        verify(mockAccountManagementDAO, times(1)).findAccountByName(anyString());
        verify(mockAccountManagementDAO, times(1)).removeRole(any(UUID.class), anyInt());

    }

    @Test
    void changeAccountStatusToEnabledSuccessfully() {
        var request = new ChangeAccountStatusRequest();
        request.setName("UKE11111");
        request.setStatus(AccountStatus.ENABLED);

        when(mockAccountManagementDAO.enableAccount(anyString(), any(AccountStatus.class)))
                .thenReturn(1);

        userManagementService.changeAccountStatus(request);

        verify(mockAccountManagementDAO, times(1)).enableAccount(anyString(), any(AccountStatus.class));
    }

    @Test
    void changeAccountStatusToDisabledSuccessfully() {
        var request = new ChangeAccountStatusRequest();
        request.setName("UKE11111");
        request.setStatus(AccountStatus.DISABLED);

        when(mockAccountManagementDAO.disableAccount(anyString(), any(AccountStatus.class)))
                .thenReturn(1);

        userManagementService.changeAccountStatus(request);

        verify(mockAccountManagementDAO, times(1)).disableAccount(anyString(), any(AccountStatus.class));
    }

    @Test
    void getNextPageTokenSuccessfully() {
        var nextPageToken = userManagementService.getNextPageToken(1, 50);
        assertEquals(2, nextPageToken);

        nextPageToken = userManagementService.getNextPageToken(0, 10);
        assertEquals(0, nextPageToken);
    }

    @Test
    void findAccountByIamIdSuccessfully() {
        when(mockAccountManagementDAO.findAccountByIamId(anyString()))
                .thenReturn(buildAccount());

        userManagementService.findAccountByIamId("UKE11111");

        verify(mockAccountManagementDAO, times(1)).findAccountByIamId(anyString());
    }

    @Test
    void findAccountByColleagueUuidSuccessfully() {
        when(mockAccountManagementDAO.findAccountByColleagueUuid(any(UUID.class)))
                .thenReturn(buildAccount());

        userManagementService.findAccountByColleagueUuid(UUID.randomUUID());

        verify(mockAccountManagementDAO, times(1)).findAccountByColleagueUuid(any(UUID.class));

    }

}