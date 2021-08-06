package com.tesco.pma.configuration.web.client;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RestTemplateCustomizer} that configures the {@link RestTemplate} to set
 * tracing header.
 *
 * @see TracingClientHttpRequestInterceptor
 */
public class TracingRestTemplateCustomizer implements RestTemplateCustomizer {
    private final TracingClientHttpRequestInterceptor interceptor;

    public TracingRestTemplateCustomizer(TracingClientHttpRequestInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
        final var alreadyPresent = existingInterceptors.stream()
                .anyMatch(TracingClientHttpRequestInterceptor.class::isInstance);
        if (!alreadyPresent) {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(existingInterceptors.size() + 1);
            interceptors.add(interceptor);
            interceptors.addAll(existingInterceptors);
            restTemplate.setInterceptors(interceptors);
        }
    }
}
