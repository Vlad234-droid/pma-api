package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Colleague not found in PMA database and Colleague Facts API
     */
    SECURITY_COLLEAGUE_NOT_FOUND,

    /**
     * Role for account already added
     */
    SECURITY_DUPLICATED_ROLE;

    @Override
    public String getCode() {
        return name();
    }

}
