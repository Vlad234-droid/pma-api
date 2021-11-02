package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.dao.RoleManagementDAO;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.AssignRoleRequest;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.domain.request.RemoveRoleRequest;
import com.tesco.pma.colleague.security.exception.ErrorCodes;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


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

    @Value("${tesco.application.user-management.page.size:50}")
    private int defaultPageLimit;

    @Override
    public List<Role> getRoles() {
        return roleManagementDAO.get();
    }

    /**
     * For more information:
     * @see <a href="https://github.dev.global.tesco.org/97-TeamTools/Colleague-Authentication-and-Access/wiki/API-pagination-options">here</a>
     *
     */
    @Override
    public List<Account> getAccounts(int page) {
        Integer offset = (page - 1) * defaultPageLimit;
        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setOffset(offset);
        requestQuery.setLimit(defaultPageLimit);
        List<Account> accounts = accountManagementDAO.get(requestQuery);
        return refinementAccounts(accounts);
    }

    @Override
    public int getNextPageToken(int currentPageToken, int currentSelectionOfAccountsSize) {
        if (currentSelectionOfAccountsSize == defaultPageLimit) {
            return currentPageToken + 1;
        }
        return 0;
    }

    @Override
    @Transactional
    public void createAccount(CreateAccountRequest request) {

        // TODO Waiting for qualification of requirements
        // if (findColleagueByIamIdOrAccountName(request.getName(), request.getIamId()).isEmpty()) {
        //    throw colleagueNotFoundException(request.getName(), request.getIamId());
        // }

        try {
            accountManagementDAO.create(request.getName(), request.getIamId(),
                    request.getStatus(), request.getType());
        } catch (DuplicateKeyException e) {
            throw duplicatedAccountException(e, request.getName());
        }

        Collection<String> roles = remappingRoles(request.getRole());
        if (!roles.isEmpty()) {
            updateRoles(true, request.getName(), roles);
        }

    }

    @Override
    @Transactional
    public void grantRole(AssignRoleRequest request) {
        Collection<String> roles = remappingRoles(request.getRole());
        updateRoles(true, request.getAccountName(), roles);
    }

    @Override
    @Transactional
    public void revokeRole(RemoveRoleRequest request) {
        Collection<String> roles = remappingRoles(request.getRole());
        updateRoles(false, request.getAccountName(), roles);
    }

    @Override
    @Transactional
    public void changeAccountStatus(ChangeAccountStatusRequest request) {
        if (AccountStatus.ENABLED.equals(request.getStatus())) {
            accountManagementDAO.enableAccount(request.getName(), AccountStatus.ENABLED);
        } else {
            accountManagementDAO.disableAccount(request.getName(), AccountStatus.DISABLED);
        }
    }

    private List<Account> refinementAccounts(List<Account> accounts) {
        return accounts.stream().peek(account -> {
            Collection<Role> roles = account.getRoles();
            if (roles.isEmpty()) {
                account.setRoles(null);
            } else if (roles.size() == 1) {
                String roleId = String.valueOf(roles.iterator().next().getId());
                account.setRole(roleId);
                account.setRoles(null);
            }
        }).collect(Collectors.toList());
    }

    /**
     *
     * @param role - Acceptable values in input json are String or list of String
     * @return
     */
    private Collection<String> remappingRoles(Object role) {
        Collection<String> retValue = new HashSet<>();
        if (role != null) {
            if (Collection.class.isAssignableFrom(role.getClass())) {
                retValue.addAll((Collection) role);
            } else if (role instanceof String) {
                retValue.add((String) role);
            }
        }
        return retValue;
    }

    private void updateRoles(boolean granted, String accountName, Collection<String> roles) {
        Optional<Account> optionalAccount = findAccountByName(accountName);
        if (optionalAccount.isEmpty()) {
            throw accountNotFoundException(accountName);
        }

        UUID accountId = optionalAccount.get().getId();
        for (String roleId : roles) {
            try {
                if (granted) {
                    accountManagementDAO.assignRole(accountId, Integer.parseInt(roleId));
                } else {
                    accountManagementDAO.removeRole(accountId, Integer.parseInt(roleId));
                }
            } catch (DuplicateKeyException e) {
                throw duplicatedRoleException(e, accountName, roleId);
            }
        }

    }

    private Optional<Account> findAccountByName(String name) {
        Account account = accountManagementDAO.findAccountByName(name);
        return Optional.ofNullable(account);
    }

    /**
     * To check the account through PMA database and Colleague Facts API
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

    private NotFoundException colleagueNotFoundException(String accountName, String iamId) {
        return new NotFoundException(ErrorCodes.SECURITY_COLLEAGUE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.SECURITY_COLLEAGUE_NOT_FOUND,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName,
                                IAM_ID_PARAMETER_NAME, iamId)));
    }

    private NotFoundException accountNotFoundException(String accountName) {
        return new NotFoundException(ErrorCodes.SECURITY_ACCOUNT_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.SECURITY_ACCOUNT_NOT_FOUND,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName)));
    }

    private DatabaseConstraintViolationException duplicatedAccountException(DuplicateKeyException exception,
                                                                            String accountName) {
        throw new DatabaseConstraintViolationException(ErrorCodes.SECURITY_ACCOUNT_ALREADY_EXISTS.name(),
                messages.getMessage(ErrorCodes.SECURITY_ACCOUNT_ALREADY_EXISTS,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName)), null, exception);
    }

    private DatabaseConstraintViolationException duplicatedRoleException(DuplicateKeyException exception,
                                                                         String accountName, String roleName) {
        return new DatabaseConstraintViolationException(ErrorCodes.SECURITY_DUPLICATED_ROLE.name(),
                messages.getMessage(ErrorCodes.SECURITY_DUPLICATED_ROLE,
                        Map.of(ACCOUNT_NAME_PARAMETER_NAME, accountName,
                                ROLE_NAME_PARAMETER_NAME, roleName
                        )), null, exception);
    }

}
