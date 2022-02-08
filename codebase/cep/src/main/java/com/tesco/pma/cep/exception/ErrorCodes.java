package com.tesco.pma.cep.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Colleague not found
     */
    COLLEAGUE_NOT_FOUND,

    /**
     * Invalid feed Id
     */
    EVENT_FEED_ID_ERROR;

    @Override
    public String getCode() {
        return name();
    }

}