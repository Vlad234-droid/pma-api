package com.tesco.pma.flow.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {
    BPM_INCORRECT_PARAMETER,
    PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE,
    EVENT_COLLEAGUE_UUID_ABSENT,
    EVENT_INVALID_COLLEAGUE_UUID_FORMAT;

    @Override
    public String getCode() {
        return name();
    }
}
