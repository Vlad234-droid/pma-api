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
     * Returns a list of accounts
     *
     * @param requestQuery
     * @return a list of accounts
     */
    List<Account> get(@Param("requestQuery") RequestQuery requestQuery);

    /**
     *
     * @param name
     * @param status
     * @return
     */
    default int disableAccount(String name, AccountStatus status) {
        return disableAccount(name, status, now());
    }

    /**
     *
     * @param name
     * @param status
     * @param now
     * @return
     */
    int disableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

    /**
     *
     * @param name
     * @param status
     * @return
     */
    default int enableAccount(String name, AccountStatus status) {
        return enableAccount(name, status, now());
    }

    /**
     *
     * @param name
     * @param status
     * @param now
     * @return
     */
    int enableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

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

    /**
     *
     * @return
     */
    long getCount();
}
