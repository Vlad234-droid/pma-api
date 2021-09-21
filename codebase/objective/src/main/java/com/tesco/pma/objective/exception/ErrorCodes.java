package com.tesco.pma.objective.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Personal objective not found
     */
    PERSONAL_OBJECTIVE_NOT_FOUND,
    /**
     * Parsing config already exists
     */
    PERSONAL_OBJECTIVE_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }
}
