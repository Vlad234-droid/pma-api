package com.tesco.pma.logging;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.api.CodeAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class LoggableService {

    private NamedMessageSourceAccessor messageSourceAccessor;

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode]
     *
     * @param codeAware - error code
     * @return formatted message
     */
    public String formatMessage(CodeAware codeAware) {
        return LogFormatter.formatMessage(messageSourceAccessor, codeAware);
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode]
     *
     * @param params    - params to retrieve message
     * @param codeAware - error code
     * @return formatted message
     */
    public String formatMessage(CodeAware codeAware, Map<String, ?> params) {
        return LogFormatter.formatMessage(messageSourceAccessor, codeAware, params);
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode] DefaultMessage
     *
     * @param codeAware      - error code
     * @param defaultMessage - default message
     * @return formatted message
     */
    public String formatMessage(CodeAware codeAware, String defaultMessage) {
        return LogFormatter.formatMessage(messageSourceAccessor, codeAware, defaultMessage);
    }

    /**
     * Tries to get message from message source and format it to be [ErrorCode] Message
     * If message was not found, return only [ErrorCode] DefaultMessage
     *
     * @param codeAware      - error code
     * @param params         - params to retrieve message
     * @param defaultMessage - default message
     * @return formatted message
     */
    public String formatMessage(CodeAware codeAware, Map<String, ?> params, String defaultMessage) {
        return LogFormatter.formatMessage(messageSourceAccessor, codeAware, params, defaultMessage);
    }

    @Autowired
    public void setMessageSourceAccessor(NamedMessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }
}
