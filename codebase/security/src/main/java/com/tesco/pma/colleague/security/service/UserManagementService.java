package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;

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
     * @param account
     */
    void createAccount(Account account);

    /**
     *
     * @param role
     */
    void grantRole(Role role);

    /**
     *
     * @param role
     */
    void revokeRole(Role role);

    /**
     *
     * @param account
     */
    void disableAccount(Account account);

    /**
     *
     * @param account
     */
    void enableAccount(Account account);

}
