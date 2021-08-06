package com.tesco.pma.logging.configuration;

import com.tesco.pma.logging.filter.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.Collections;

/**
 * Request logger configuration.
 */
@Configuration
public class RequestLoggerConfiguration {

    private static final String MESSAGE_PREFIX = "REQUEST DATA: [";
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    /**
     * Configures the request Logging Filter
     * @return RequestLoggingFilter
     */
    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        var loggingFilter = new RequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(false);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setMessagePrefix(MESSAGE_PREFIX);
        loggingFilter.setMaxPayloadLength(MAX_PAYLOAD_LENGTH);
        loggingFilter.setAllowedRequestMediaType(Collections.singleton(MediaType.APPLICATION_JSON));
        loggingFilter.setAllowedResponseMediaType(Collections.singleton(MediaType.APPLICATION_JSON));
        return loggingFilter;
    }

}
