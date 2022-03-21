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
     * Profile attribute not found
     */
    PROFILE_ATTRIBUTE_NOT_FOUND,

    /**
     * Profile attribute name already exists
     */
    PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS,
    COLLEAGUES_MANAGER_NOT_FOUND,
    INVALID_COLLEAGUE_IDENTIFIER,
    IMPORT_REQUEST_NOT_FOUND,

    /**
     * Colleague not found
     */
    COLLEAGUE_NOT_FOUND,

    /**
     * Manager not found
     */
    MANAGER_NOT_FOUND,

    /**
     * Duplicate key exception
     */
    DATA_INTEGRITY_VIOLATION_EXCEPTION;

    @Override
    public String getCode() {
        return name();
    }

}
