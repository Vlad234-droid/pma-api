package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.*;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public interface AccountManagementDAO {

    /**
     *
     * @param request
     * @return
     */
    default int create(final CreateAccountRequest request) {
        return create(request, now());
    }

    /**
     *
     * @param request
     * @param now
     * @return
     */
    int create(@Param("request") final CreateAccountRequest request, @Param("now") Instant now);

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
