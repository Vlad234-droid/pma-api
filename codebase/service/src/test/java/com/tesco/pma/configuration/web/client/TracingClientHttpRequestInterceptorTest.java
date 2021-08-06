package com.tesco.pma.configuration.web.client;

import com.tesco.pma.logging.TraceId;
import com.tesco.pma.logging.TraceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TracingClientHttpRequestInterceptorTest {

    private TracingClientHttpRequestInterceptor instance;
    private String traceIdHeaderName = TraceId.TRACE_ID_HEADER;

    @BeforeEach
    void setUp() {
        instance = new TracingClientHttpRequestInterceptor();
    }

    @Test
    void interceptShouldAddHeaderIfWasNotPresented() throws Exception {
        final var currentTraceId = TraceUtils.getTraceId();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = {};

        instance.intercept(request, body, execution);

        verify(execution).execute(request, body);
        traceIdHeaderName = TraceId.TRACE_ID_HEADER;
        assertThat(request.getHeaders().getFirst(traceIdHeaderName)).isEqualTo(currentTraceId.getValue());
    }

    @Test
    void interceptShouldSkipAddingHeaderIfAlreadySet() throws Exception {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET);
        request.getHeaders().set(traceIdHeaderName, "already present");
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = {};

        instance.intercept(request, body, execution);

        verify(execution).execute(request, body);
        assertThat(request.getHeaders().getFirst(traceIdHeaderName)).isEqualTo("already present");
    }
}