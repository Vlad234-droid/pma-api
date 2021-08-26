package com.tesco.pma.profile.exception;

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
    PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }

}
