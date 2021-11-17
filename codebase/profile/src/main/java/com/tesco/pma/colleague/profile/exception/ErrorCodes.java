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
    COLLEAGUES_MANAGER_NOT_FOUND,
    IMPORT_REQUEST_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }

}
