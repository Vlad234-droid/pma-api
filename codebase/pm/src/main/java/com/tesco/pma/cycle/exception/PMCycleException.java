package com.tesco.pma.cycle.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class PMCycleException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 243330833393343711L;

    public PMCycleException(String code, String message, String field) {
        super(code, message, field);
    }

    public PMCycleException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
