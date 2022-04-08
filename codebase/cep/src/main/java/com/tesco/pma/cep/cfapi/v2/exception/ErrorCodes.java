package com.tesco.pma.cep.cfapi.v2.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Colleague not found
     */
    COLLEAGUE_NOT_FOUND,

    /**
     * Changed attributes not found
     */
    CHANGED_ATTRIBUTES_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }

}