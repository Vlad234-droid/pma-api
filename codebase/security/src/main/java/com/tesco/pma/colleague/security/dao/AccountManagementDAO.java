package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;

public interface AccountManagementDAO {

    /**
     * Create account
     *
     * @param name
     * @param iamId
     * @param status
     * @param type
     * @return
     */
    default int create(String name, String iamId, AccountStatus status, AccountType type) {
        return create(name, iamId, status, type,now());
    }

    /**
     * Create account
     *
     * @param name
     * @param iamId
     * @param status
     * @param type
     * @param now
     * @return
     */
    int create(@Param("name") String name,
               @Param("iamId") String iamId,
               @Param("status") AccountStatus status,
               @Param("type") AccountType type,
               @Param("now") Instant now);

    /**
     * Returns users, their status and access levels
     *
     * @param requestQuery
     * @return a list of accounts
     */
    List<Account> get(@Param("requestQuery") RequestQuery requestQuery);

    /**
     * Disable account
     *
     * @param name
     * @param status
     * @return
     */
    default int disableAccount(String name, AccountStatus status) {
        return disableAccount(name, status, now());
    }

    /**
     * Disable account
     *
     * @param name
     * @param status
     * @param now
     * @return
     */
    int disableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

    /**
     * Enable account
     *
     * @param name
     * @param status
     * @return
     */
    default int enableAccount(String name, AccountStatus status) {
        return enableAccount(name, status, now());
    }

    /**
     * Enable account
     *
     * @param name
     * @param status
     * @param now
     * @return
     */
    int enableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

    /**
     * Add access
     *
     * @param accountId
     * @param roleId
     * @return
     */
    int assignRole(@Param("accountId") final long accountId, @Param("roleId") final int roleId);

    /**
     * Remove access
     *
     * @param accountId
     * @param roleId
     * @return
     */
    int removeRole(@Param("accountId") final long accountId, @Param("roleId") final int roleId);

    /**
     * Find account by account name
     *
     * @param name
     * @return
     */
    Account findAccountByName(@Param("name") String name);

    /**
     * Get total number of accounts
     *
     * @return
     */
    long getCount();
}
