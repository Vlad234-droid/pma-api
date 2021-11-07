package com.tesco.pma.colleague.profile.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Profile not found
     */
    PROFILE_NOT_FOUND,

    /**
     * Profile attribute already exists
     */
    PROFILE_ATTRIBUTE_ALREADY_EXISTS,

    /**
     * Profile attribute name already exists
     */
    PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS,

    /**
     * Duplicate key exception
     */
    DATA_INTEGRITY_VIOLATION_EXCEPTION;

    @Override
    public String getCode() {
        return name();
    }

}
