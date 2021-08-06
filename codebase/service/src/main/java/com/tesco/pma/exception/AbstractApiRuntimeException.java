package com.tesco.pma.exception;

import com.tesco.pma.api.CodeAware;
import com.tesco.pma.api.TargetAware;

/**
 *  General abstract class for app specific exceptions
 */
public abstract class AbstractApiRuntimeException extends RuntimeException implements CodeAware, TargetAware {

    protected final String code;
    protected final String field;

    protected AbstractApiRuntimeException(String code, String message) {
        this(code, message, null);
    }

    protected AbstractApiRuntimeException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    protected AbstractApiRuntimeException(String code, String message, String field, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.field = field;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getTarget() {
        return field;
    }
}
