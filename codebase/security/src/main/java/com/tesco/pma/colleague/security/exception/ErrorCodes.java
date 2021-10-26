package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Colleague not found in PMA database and Colleague Facts API
     */
    COLLEAGUE_NOT_FOUND,

    /**
     * Role for account already added
     */
    DUPLICATED_ROLE;

    @Override
    public String getCode() {
        return name();
    }

}
