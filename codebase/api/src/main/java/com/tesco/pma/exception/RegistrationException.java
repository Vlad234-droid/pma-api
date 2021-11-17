package com.tesco.pma.exception;

import lombok.Getter;

@Getter
public class RegistrationException extends AbstractApiRuntimeException {

    public RegistrationException(String code, String message, String dataRequestName) {
        super(code, message, dataRequestName);
    }

    public RegistrationException(String code, String message, String dataRequestName, Throwable cause) {
        super(code, message, dataRequestName, cause);
    }

}
