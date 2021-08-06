package com.tesco.pma.logging;

import com.tesco.pma.logging.filter.RequestLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
@Endpoint(id = "requestLogger")
public class RequestLoggerEndpoint {

    private final RequestLoggingFilter filter;

    @WriteOperation
    public void setConfig(@Selector RequestLoggingAction property, Boolean value) {
        switch (property) {
            case INCLUDE_PAYLOAD:
                filter.setIncludePayload(value);
                break;
            case INCLUDE_QUERY_STRING:
                filter.setIncludeQueryString(value);
                break;
            case INCLUDE_CLIENT_INFO:
                filter.setIncludeClientInfo(value);
                break;
            case INCLUDE_HEADERS:
                filter.setIncludeHeaders(value);
                break;
            default:
                throw new IllegalArgumentException("Action for property is not set");
        }
    }

    @ReadOperation
    public Map<RequestLoggingAction, Boolean> getProperties() {
        return Map.of(RequestLoggingAction.INCLUDE_PAYLOAD, filter.isIncludePayload(),
                RequestLoggingAction.INCLUDE_QUERY_STRING, filter.isIncludeQueryString(),
                RequestLoggingAction.INCLUDE_CLIENT_INFO, filter.isIncludeClientInfo(),
                RequestLoggingAction.INCLUDE_HEADERS, filter.isIncludeHeaders());
    }
}
