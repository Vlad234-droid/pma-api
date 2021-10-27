package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.dao.RoleManagementDAO;
import com.tesco.pma.colleague.security.domain.*;
import com.tesco.pma.colleague.security.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link UserManagementService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final AccountManagementDAO accountManagementDAO;
    private final RoleManagementDAO roleManagementDAO;

    private final ConfigEntryService configEntryService;

    private final NamedMessageSourceAccessor messages;

    private static final String ACCOUNT_NAME_PARAMETER_NAME = "accountName";
    private static final String IAM_ID_PARAMETER_NAME = "iamId";
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
        if (!findColleagueByIamIdOrAccountName(request.getName(), request.getIamId()).isEmpty()) {
            throw colleagueNotFound(request.getName(), request.getIamId());
        }

        int inserted = accountManagementDAO.create(request);

        if (!request.getRoles().isEmpty()) {
            updateRoles(true, request.getName(), request.getRoles());
        }
    }

    @Override
    @Transactional
    public void grantRole(AssignRoleRequest request) {
        updateRoles(true, request.getAccountName(), request.getRoles());
    }

    @Override
    @Transactional
    public void revokeRole(RemoveRoleRequest request) {
        updateRoles(false, request.getAccountName(), request.getRoles());
    }

    private void updateRoles(boolean granted, String accountName, Collection<String> roles) {
        Optional<Account> optionalAccount = findAccountByName(accountName);
        if (optionalAccount.isEmpty()) {
            // TODO
            throw new NotFoundException("", "");
        }

        try {
            long accountId = optionalAccount.get().getId();
            for (String roleId : roles) {
                if (granted) {
                    accountManagementDAO.assignRole(accountId, Integer.parseInt(roleId));
                } else {
                    accountManagementDAO.removeRole(accountId, Integer.parseInt(roleId));
                }
            }
        } catch (DataIntegrityViolationException e) {
            // TODO
            throw new DatabaseConstraintViolationException("", "", "");
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

    private Optional<Account> findAccountByName(String name) {
        Account account = accountManagementDAO.findAccountByName(name);
        return Optional.ofNullable(account);
    }

    /**
     *  To check the account through PMA database and Colleague Facts API
     *
     * @param accountName
     * @param iamId
     * @return
     */
    private Optional<Colleague> findColleagueByIamIdOrAccountName(String accountName, String iamId) {
        Colleague colleague = configEntryService.getColleagueByIamId(iamId);
        if (colleague == null) {
            colleague = configEntryService.getColleagueByIamId(accountName);
        }

        return Optional.ofNullable(colleague);
    }

    private NotFoundException colleagueNotFound(String accountName, String iamId) {
        return new NotFoundException(ErrorCodes.SECURITY_COLLEAGUE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.SECURITY_COLLEAGUE_NOT_FOUND,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName,
                                IAM_ID_PARAMETER_NAME, iamId)));
    }

    private DatabaseConstraintViolationException duplicatedRole(DuplicateKeyException e,
                                                                String accountName, String roleName) {
        return new DatabaseConstraintViolationException(ErrorCodes.SECURITY_DUPLICATED_ROLE.name(),
                messages.getMessage(ErrorCodes.SECURITY_DUPLICATED_ROLE,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName,
                                ROLE_NAME_PARAMETER_NAME, roleName
                        )), null, e);
    }

}
