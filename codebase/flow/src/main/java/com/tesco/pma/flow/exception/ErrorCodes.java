package com.tesco.pma.flow.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {
    BPM_INCORRECT_PARAMETER,
    PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE,
    EVENT_COLLEAGUE_UUID_ABSENT,
    EVENT_INVALID_COLLEAGUE_UUID_FORMAT,
    PARAMETER_CANNOT_BE_READ,
    PM_CYCLE_MORE_THAN_ONE_IN_STATUSES,
    PM_CYCLE_ASSIGNED_FOR_COLLEAGUE,
    PM_CYCLE_NOT_ASSIGNED_FOR_COLLEAGUE;

    @Override
    public String getCode() {
        return name();
    }
}
