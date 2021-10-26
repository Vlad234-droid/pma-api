package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.domain.*;

import java.util.List;

/**
 * User management service
 */
public interface UserManagementService {

    /**
     *
     * @return
     */
    List<Role> getRoles();

    /**
     *
     * @return
     */
    List<Account> getAccounts();

    /**
     *
     * @param request
     */
    void createAccount(CreateAccountRequest request);

    /**
     *
     * @param request
     */
    void grantRole(AssignRoleRequest request);

    /**
     *
     * @param request
     */
    void revokeRole(RemoveRoleRequest request);

    /**
     *
     * @param request
     */
    void disableAccount(DisableAccountRequest request);

    /**
     *
     * @param request
     */
    void enableAccount(EnableAccountRequest request);

}
