package com.tesco.pma.objective.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Personal objective not found by uuid
     */
    PERSONAL_OBJECTIVE_NOT_FOUND_BY_UUID,
    /**
     * Personal objective not found for colleague
     */
    PERSONAL_OBJECTIVE_NOT_FOUND_FOR_COLLEAGUE,
    /**
     * Personal objectives not found
     */
    PERSONAL_OBJECTIVES_NOT_FOUND,
    /**
     * Personal objective already exists
     */
    PERSONAL_OBJECTIVE_ALREADY_EXISTS,
    /**
     * Business unit does not exists
     */
    BUSINESS_UNIT_NOT_EXISTS,
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
