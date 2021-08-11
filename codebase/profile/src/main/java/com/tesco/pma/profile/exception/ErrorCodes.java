package com.tesco.pma.profile.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Profile not found
     */
    PROFILE_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }

}
