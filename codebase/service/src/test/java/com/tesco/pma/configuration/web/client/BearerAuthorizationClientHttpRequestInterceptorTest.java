package com.tesco.pma.configuration.web.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BearerAuthorizationClientHttpRequestInterceptorTest {

    @Test
    void interceptShouldAddHeaderIfWasNotPresented() throws Exception {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = {};

        new BearerAuthorizationClientHttpRequestInterceptor(() -> "some-token").intercept(request, body, execution);

        verify(execution).execute(request, body);
        assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("Bearer some-token");
    }

    @Test
    void interceptShouldSkipAddingHeaderIfAlreadySet() throws Exception {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET);
        request.getHeaders().set("Authorization", "already present");
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = {};

        new BearerAuthorizationClientHttpRequestInterceptor(() -> "some-token").intercept(request, body, execution);

        verify(execution).execute(request, body);
        assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("already present");
    }
}