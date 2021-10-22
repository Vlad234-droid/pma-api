package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.domain.Account;
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

    @Override
    public List<Role> getRoles() {
        return List.of();
    }

    @Override
    public List<Account> getAccounts() {
        return List.of();
    }

    @Override
    public void createAccount(Account account) {

    }

    @Override
    public void grantRole(Role role) {

    }

    @Override
    public void revokeRole(Role role) {

    }

    @Override
    public void disableAccount(Account account) {

    }

    @Override
    public void enableAccount(Account account) {

    }

}
