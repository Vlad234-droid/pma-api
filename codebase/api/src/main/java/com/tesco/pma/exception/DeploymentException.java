package com.tesco.pma.exception;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.06.2021 Time: 19:35
 */
public class DeploymentException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = -3844121883213725125L;

    public DeploymentException(String code, String message) {
        super(code, message);
    }

    public DeploymentException(String code, String message, String field) {
        super(code, message, field);
    }

    public DeploymentException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
