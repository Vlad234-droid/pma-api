package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.DisableAccountRequest;
import com.tesco.pma.colleague.security.domain.EnableAccountRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AccountManagementDAO {

    int create(@Param("account") final Account account);

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

}
