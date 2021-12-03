package com.tesco.pma.flow.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {
    BPM_INCORRECT_PARAMETER;

    @Override
    public String getCode() {
        return name();
    }
}
