package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.dao.RoleManagementDAO;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.DisableAccountRequest;
import com.tesco.pma.colleague.security.domain.EnableAccountRequest;
import com.tesco.pma.colleague.security.domain.Role;
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
    public void createAccount(Account account) {
        int inserted = accountManagementDAO.create(account);
    }

    @Override
    public void grantRole(Role role) {

    }

    @Override
    public void revokeRole(Role role) {

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
