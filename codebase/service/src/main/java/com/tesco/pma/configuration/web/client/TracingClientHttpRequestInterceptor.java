package com.tesco.pma.configuration.web.client;

import com.tesco.pma.logging.TraceId;
import com.tesco.pma.logging.TraceUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * {@link ClientHttpRequestInterceptor} to apply a {@code TraceId} header,
 * unless a custom {@code TraceId} header has already been set.
 *
 * @see TraceUtils#getTraceId
 */
public class TracingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!request.getHeaders().containsKey(TraceId.TRACE_ID_HEADER)) {
            request.getHeaders().set(TraceId.TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());
        }
        return execution.execute(request, body);
    }
}
