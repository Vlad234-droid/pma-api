package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.dao.RoleManagementDAO;
import com.tesco.pma.colleague.security.domain.*;
import com.tesco.pma.colleague.security.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link UserManagementService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final AccountManagementDAO accountManagementDAO;
    private final RoleManagementDAO roleManagementDAO;

    private final NamedMessageSourceAccessor messages;

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String ACCOUNT_NAME_PARAMETER_NAME = "accountName";
    private static final String ROLE_NAME_PARAMETER_NAME = "roleName";

    @Override
    @Transactional(readOnly = true)
    public List<Role> getRoles() {
        return roleManagementDAO.get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccounts() {
        return accountManagementDAO.get();
    }

    @Override
    @Transactional
    public void createAccount(CreateAccountRequest request) {

        // TODO
        if (!accountExists(request)) {
            throw colleagueNotFound(request.getStatus());
        }

        int inserted = accountManagementDAO.create(request);
        for (String role : request.getRoles()) {
            accountManagementDAO.assignRole(request.getName(), role);
        }
    }

    @Override
    @Transactional
    public void grantRole(AssignRoleRequest request) {
        for (String role : request.getRoles()) {
            accountManagementDAO.assignRole(request.getAccountName(), role);
        }
    }

    @Override
    @Transactional
    public void revokeRole(RemoveRoleRequest request) {
        for (String role : request.getRoles()) {
            accountManagementDAO.removeRole(request.getName(), role);
        }
    }

    @Override
    @Transactional
    public void disableAccount(DisableAccountRequest request) {
        int updated = accountManagementDAO.disableAccount(request);
    }

    @Override
    @Transactional
    public void enableAccount(EnableAccountRequest request) {
        int updated = accountManagementDAO.enableAccount(request);
    }

    // TODO
    private boolean accountExists(CreateAccountRequest request) {
        return true;
    }

    private NotFoundException colleagueNotFound(String colleague) {
        return new NotFoundException(ErrorCodes.COLLEAGUE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.COLLEAGUE_NOT_FOUND,
                        Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleague)));
    }

    private DatabaseConstraintViolationException duplicatedRole(DuplicateKeyException e,
                                                                String accountName, String roleName) {
        return new DatabaseConstraintViolationException(ErrorCodes.DUPLICATED_ROLE.name(),
                messages.getMessage(ErrorCodes.DUPLICATED_ROLE,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName,
                                ROLE_NAME_PARAMETER_NAME, roleName
                        )), null, e);
    }

}
