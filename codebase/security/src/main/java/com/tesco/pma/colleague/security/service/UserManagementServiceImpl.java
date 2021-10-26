package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.dao.RoleManagementDAO;
import com.tesco.pma.colleague.security.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Implementation of {@link UserManagementService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final AccountManagementDAO accountManagementDAO;
    private final RoleManagementDAO roleManagementDAO;

    @Override
    public List<Role> getRoles() {
        return roleManagementDAO.get();
    }

    @Override
    public List<Account> getAccounts() {
        return accountManagementDAO.get();
    }

    @Override
    public void createAccount(CreateAccountRequest request) {
        int inserted = accountManagementDAO.create(request);
        for (String role : request.getRoles()) {
            accountManagementDAO.assignRole(request.getName(), role);
        }
    }

    @Override
    public void grantRole(AssignRoleRequest request) {
        for (String role : request.getRoles()) {
            accountManagementDAO.assignRole(request.getAccountName(), role);
        }
    }

    @Override
    public void revokeRole(RemoveRoleRequest request) {
        for (String role : request.getRoles()) {
            accountManagementDAO.removeRole(request.getName(), role);
        }
    }

    @Override
    public void disableAccount(DisableAccountRequest request) {
        int updated = accountManagementDAO.disableAccount(request);
    }

    @Override
    public void enableAccount(EnableAccountRequest request) {
        int updated = accountManagementDAO.enableAccount(request);
    }

}
