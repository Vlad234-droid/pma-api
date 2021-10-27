package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AccountManagementDAO {

    int create(@Param("request") final CreateAccountRequest request);

    /**
     * Returns a list of accounts
     *
     * @return a list of accounts
     */
    List<Account> get();

    /**
     *
     * @param request
     * @return
     */
    int disableAccount(@Param("request") final DisableAccountRequest request);

    /**
     *
     * @param request
     * @return
     */
    int enableAccount(@Param("request") final EnableAccountRequest request);

    /**
     *
     * @param accountId
     * @param roleId
     * @return
     */
    int assignRole(@Param("accountId") final long accountId, @Param("roleId") final int roleId);

    /**
     *
     * @param accountId
     * @param roleId
     * @return
     */
    int removeRole(@Param("accountId") final long accountId, @Param("roleId") final int roleId);

    /**
     *
     * @param name
     * @return
     */
    Account findAccountByName(@Param("name") String name);

}
