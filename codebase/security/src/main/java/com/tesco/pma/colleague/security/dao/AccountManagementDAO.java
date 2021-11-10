package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

/**
 * Interface to perform database operation on {@link Role}, {@link Account}.
 */
public interface AccountManagementDAO {

    /**
     * Returns an available access levels & metadata
     *
     * @return a list of roles
     */
    List<Role> findAllRoles();

    /**
     * Create account
     *
     * @param name
     * @param iamId
     * @param status
     * @param type
     * @return Count of inserted records
     */
    default int create(String name, String iamId, AccountStatus status, AccountType type) {
        return create(UUID.randomUUID(), name, iamId, status, type, now());
    }

    /**
     * Create account
     *
     * @param id
     * @param name
     * @param iamId
     * @param status
     * @param type
     * @param now
     * @return Count of inserted records
     */
    int create(@Param("id") UUID id, @Param("name") String name, @Param("iamId") String iamId,
               @Param("status") AccountStatus status, @Param("type") AccountType type, @Param("now") Instant now);

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
     * @return Count of updated records
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
     * @return Count of updated records
     */
    int disableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

    /**
     * Enable account
     *
     * @param name
     * @param status
     * @return Count of updated records
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
     * @return Count of updated records
     */
    int enableAccount(@Param("name") String name, @Param("status") AccountStatus status, @Param("now") Instant now);

    /**
     * Add access
     *
     * @param accountId
     * @param roleId
     * @return Count of updated records
     */
    int assignRole(@Param("accountId") final UUID accountId, @Param("roleId") final int roleId);

    /**
     * Remove access
     *
     * @param accountId
     * @param roleId
     * @return Count of updated records
     */
    int removeRole(@Param("accountId") final UUID accountId, @Param("roleId") final int roleId);

    /**
     * Find account by account name
     *
     * @param name
     * @return Account
     */
    Account findAccountByName(@Param("name") String name);

}
