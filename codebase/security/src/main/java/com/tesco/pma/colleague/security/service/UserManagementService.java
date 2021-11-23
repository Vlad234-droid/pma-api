package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.RoleRequest;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;

import java.util.List;
import java.util.UUID;

/**
 * User management service
 */
public interface UserManagementService {

    /**
     * Returns an available access levels & metadata
     *
     * @return An available access levels & metadata
     */
    List<Role> getRoles();

    /**
     * Returns users, their status and access levels
     *
     * @return Users, their status and access levels
     */
    List<Account> getAccounts(int page);

    /**
     * Get next page number
     *
     * @return Next page number
     */
    int getNextPageToken(int currentPageToken, int currentSelectionOfAccountsSize);

    /**
     * Create account
     *
     * @param request
     */
    void createAccount(CreateAccountRequest request);

    /**
     * Add access
     *
     * @param request
     */
    void grantRole(RoleRequest request);

    /**
     * Remove access
     *
     * @param request
     */
    void revokeRole(RoleRequest request);

    /**
     * Enable / Disable account
     *
     * @param request
     */
    void changeAccountStatus(ChangeAccountStatusRequest request);

    /**
     * Find account by IAM ID
     * @param iamId
     * @return Account information
     */
    Account findAccountByIamId(String iamId);

    /**
     * Find account by colleague UUID
     * @param colleagueUuid
     * @return Account information
     */
    Account findAccountByColleagueUuid(UUID colleagueUuid);

}
