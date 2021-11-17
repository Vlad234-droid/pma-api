package com.tesco.pma.cycle.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-15 15:56
 */
public class ParseException extends AbstractApiRuntimeException {
    public ParseException(String code, String message) {
        super(code, message);
    }

    public ParseException(String code, String message, String field) {
        super(code, message, field);
    }

    public ParseException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
