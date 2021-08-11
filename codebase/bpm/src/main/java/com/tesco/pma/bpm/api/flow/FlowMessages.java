package com.tesco.pma.bpm.api.flow;

public enum FlowMessages {
    APP_INFO,
    APP_WARNING,
    FLOW_HANDLER_START,
    FLOW_HANDLER_END,
    FLOW_HANDLER_END_ERROR,

    /* Errors */
    APP_ERROR_INVALID_PARAMETERS,
    APP_ERROR_INVALID_CONFIGURATION,

    FLOW_ERROR_INCORRECT_FLOW_INITIALIZATION,
    FLOW_ERROR_INCORRECT_EVENT,
    FLOW_ERROR_RUNTIME,

    /* Warnings */
    MAX_ITERATIONS_REACHED;

    public static String format(Enum code, String message, Object... params) {
        return String.format(code.name() + ": " + message, params);
    }

    public String format(String message, Object... params) {
        return format(this, message, params);
    }
}