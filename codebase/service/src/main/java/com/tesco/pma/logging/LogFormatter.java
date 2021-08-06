package com.tesco.pma.logging;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.api.CodeAware;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
@UtilityClass
public class LogFormatter {

    private static final String WRAP_FORMAT = "[%s]";
    private static final String TRACE_FORMAT = "[TraceID: %s]";
    private static final String LOG_FORMAT = WRAP_FORMAT + SPACE + "%s" + SPACE + "%s";
    private static final String MESSAGE_NOT_FOUND_MESSAGE_FORMAT = "Error message not found, message key: %s";

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode]
     *
     * @param messageSourceAccessor - message source
     * @param codeAware             - error code
     * @return formatted message
     */
    public String formatMessage(NamedMessageSourceAccessor messageSourceAccessor, CodeAware codeAware) {
        try {
            var message = messageSourceAccessor.getMessage(codeAware.getCode());
            return String.format(LOG_FORMAT, codeAware.getCode(), formatTraceId(), message);
        } catch (NoSuchMessageException ex) {
            log.trace(String.format(MESSAGE_NOT_FOUND_MESSAGE_FORMAT, codeAware.getCode()));
            return wrap(codeAware.getCode());
        }
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode]
     *
     * @param messageSourceAccessor - message source
     * @param params                - params to retrieve message
     * @param codeAware             - error code
     * @return formatted message
     */
    public String formatMessage(NamedMessageSourceAccessor messageSourceAccessor, CodeAware codeAware, Map<String, ?> params) {
        try {
            var message = messageSourceAccessor.getMessage(codeAware.getCode(), params);
            return String.format(LOG_FORMAT, codeAware.getCode(), formatTraceId(), message);
        } catch (NoSuchMessageException ex) {
            log.trace(String.format(MESSAGE_NOT_FOUND_MESSAGE_FORMAT, codeAware.getCode()));
            return wrap(codeAware.getCode());
        }
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode] DefaultMessage
     *
     * @param messageSourceAccessor - message source
     * @param codeAware             - error code
     * @param defaultMessage        - default message
     * @return formatted message
     */
    public String formatMessage(NamedMessageSourceAccessor messageSourceAccessor, CodeAware codeAware, String defaultMessage) {
        String message;
        try {
            message = messageSourceAccessor.getMessage(codeAware.getCode());
        } catch (NoSuchMessageException ex) {
            log.trace(String.format(MESSAGE_NOT_FOUND_MESSAGE_FORMAT, codeAware.getCode()));
            message = defaultMessage;
        }
        return String.format(LOG_FORMAT, codeAware.getCode(), formatTraceId(), message);
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode] DefaultMessage
     *
     * @param messageSourceAccessor - message source
     * @param codeAware             - error code
     * @param params                - params to retrieve message
     * @param defaultMessage        - default message
     * @return formatted message
     */
    public String formatMessage(NamedMessageSourceAccessor messageSourceAccessor, CodeAware codeAware,
                                Map<String, ?> params, String defaultMessage) {
        String message;
        try {
            message = messageSourceAccessor.getMessage(codeAware.getCode(), params);
        } catch (NoSuchMessageException ex) {
            log.trace(String.format(MESSAGE_NOT_FOUND_MESSAGE_FORMAT, codeAware.getCode()));
            message = defaultMessage;
        }
        return String.format(LOG_FORMAT, codeAware.getCode(), formatTraceId(), message);
    }

    /**
     * Formats message to be [ErrorCode] Message
     *
     * @param codeAware - error code
     * @param message   - message
     * @return formatted message
     */
    public String formatMessage(CodeAware codeAware, String message) {
        return String.format(LOG_FORMAT, codeAware.getCode(), formatTraceId(), message);
    }

    private String wrap(String message) {
        return String.format(WRAP_FORMAT, message);
    }

    /**
     * Builds log message which contains trace id and parent trace id if present
     *
     * @return formated trace id log message
     */
    private String formatTraceId() {
        var traceId = TraceUtils.getTraceId();
        if (traceId.getParent() == null) {
            return String.format(TRACE_FORMAT, traceId.getValue());
        }
        var sb = new StringBuilder();
        sb.append("[TraceID: ").append(traceId.getValue());
        while (traceId.getParent() != null) {
            traceId = traceId.getParent();
            sb.append(", Parent: ").append(traceId.getValue());
        }
        return sb.append(']').toString();
    }
}
