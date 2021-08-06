package com.tesco.pma.configuration.web.client;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RestTemplateCustomizer} that configures the {@link RestTemplate} to set
 * Bearer token in Authorization header.
 *
 * @see BearerAuthorizationClientHttpRequestInterceptor
 */
public class BearerAuthorizationRestTemplateCustomizer implements RestTemplateCustomizer {
    private final BearerAuthorizationClientHttpRequestInterceptor interceptor;

    public BearerAuthorizationRestTemplateCustomizer(BearerAuthorizationClientHttpRequestInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();

        if (!existingInterceptors.contains(interceptor)) {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(existingInterceptors.size() + 1);
            interceptors.addAll(existingInterceptors);
            interceptors.add(interceptor);
            restTemplate.setInterceptors(interceptors);
        }
    }
}
