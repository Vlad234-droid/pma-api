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

}
