package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.AssignRoleRequest;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.domain.request.RemoveRoleRequest;

import java.util.List;

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
    void grantRole(AssignRoleRequest request);

    /**
     * Remove access
     *
     * @param request
     */
    void revokeRole(RemoveRoleRequest request);

    /**
     * Enable / Disable account
     *
     * @param request
     */
    void changeAccountStatus(ChangeAccountStatusRequest request);

}
