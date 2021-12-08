package com.tesco.pma.event.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    EVENT_SENDING_ERROR;

    @Override
    public String getCode() {
        return name();
    }
}
