package com.tesco.pma.objective.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Personal objective not found
     */
    PERSONAL_OBJECTIVE_NOT_FOUND,
    /**
     * Personal objective already exists
     */
    PERSONAL_OBJECTIVE_ALREADY_EXISTS,
    /**
     * Group objective foreign constraint violation
     */
    GROUP_OBJECTIVE_FOREIGN_CONSTRAINT_VIOLATION,
    /**
     * Group objective already exists
     */
    GROUP_OBJECTIVE_ALREADY_EXISTS,
    /**
     * Group objectives not found
     */
    GROUP_OBJECTIVES_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
