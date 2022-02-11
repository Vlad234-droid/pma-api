package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.LocalTestConfig;
import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.colleague.security.TestDataUtils.buildAccount;
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
// TODO Implement all tests
class UserManagementServiceTest {

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
    void getAccounts() {
    }

    @Test
    void createAccount() {
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
    void changeAccountStatus() {
    }

    @Test
    void getNextPageToken() {
    }

    @Test
    void findAccountByIamId() {
    }

    @Test
    void findAccountByColleagueUuid() {
    }

}