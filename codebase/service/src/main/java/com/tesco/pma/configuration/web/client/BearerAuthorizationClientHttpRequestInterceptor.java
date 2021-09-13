package com.tesco.pma.configuration.web.client;

import java.io.IOException;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.NonNull;

/**
 * {@link ClientHttpRequestInterceptor} to apply a given HTTP Bearer Authentication
 * token, unless a custom {@code Authorization} header has already been set.
 *
 * @see HttpHeaders#setBearerAuth
 */
public class BearerAuthorizationClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private final Supplier<String> tokenSupplier;

    public BearerAuthorizationClientHttpRequestInterceptor(@NonNull Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            headers.setBearerAuth(tokenSupplier.get());
        }
        return execution.execute(request, body);
    }
}
