package com.tesco.pma.tip.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {
    /**
     * Tip not found
     */
    TIP_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
