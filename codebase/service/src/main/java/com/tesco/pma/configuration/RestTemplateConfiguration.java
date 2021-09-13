package com.tesco.pma.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import com.tesco.pma.configuration.web.client.BearerAuthorizationClientHttpRequestInterceptor;
import com.tesco.pma.configuration.web.client.BearerAuthorizationRestTemplateCustomizer;
import com.tesco.pma.configuration.web.client.TracingClientHttpRequestInterceptor;
import com.tesco.pma.configuration.web.client.TracingRestTemplateCustomizer;

/**
 * Default {@link RestTemplate} configuration.
 * tesco:
 *   application:
 *     rest-template:
 *       connect-timeout: 20000
 *       read-timeout: 30000
 *       http-factory-class: com.tesco.pma.configuration.RestTemplateConfigurationTest.TestFactory
 */
@Configuration("defaultRestTemplateConfiguration")
public class RestTemplateConfiguration {

    /**
     * Default {@link RestTemplate}.
     *
     * <p>Interceptors added via {@link RestTemplateBuilder}:
     * TracingClientHttpRequestInterceptor, BearerAuthorizationClientHttpRequestInterceptor.
     *
     * @return default {@link RestTemplate}
     * @see org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
     * @see RestTemplateConfiguration#tracingRestTemplateCustomizer
     * @see RestTemplateConfiguration#pmaAuthorizationRestTemplateCustomizer
     */
    @Bean
    @Primary
    public RestTemplate defaultRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                            @Value("${tesco.application.rest-template.connect-timeout:5000}") int connectTimeout,
                                            @Value("${tesco.application.rest-template.read-timeout:30000}") int readTimeout,
                                            @Value("${tesco.application.rest-template.http-factory-class:#{null}}")
                                                    Optional<Class<? extends ClientHttpRequestFactory>> factoryClass) {
        return factoryClass.map(restTemplateBuilder::requestFactory)
                .orElse(restTemplateBuilder)
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    @Bean
    public RestTemplateCustomizer tracingRestTemplateCustomizer() {
        return new TracingRestTemplateCustomizer(new TracingClientHttpRequestInterceptor());
    }

    @Bean
    @ConditionalOnProperty(prefix = "tesco.application", name = "rest-template.security.enabled")
    public RestTemplateCustomizer pmaAuthorizationRestTemplateCustomizer(
            BearerAuthorizationClientHttpRequestInterceptor interceptor) {
        return new BearerAuthorizationRestTemplateCustomizer(interceptor);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tesco.application", name = "rest-template.security.enabled")
    public BearerAuthorizationClientHttpRequestInterceptor pmaClientBearerAuthorizationRequestInterceptor(
            @Qualifier("pmaClientTokenSupplier") Supplier<String> tokenSupplier) {
        return new BearerAuthorizationClientHttpRequestInterceptor(tokenSupplier);
    }

}