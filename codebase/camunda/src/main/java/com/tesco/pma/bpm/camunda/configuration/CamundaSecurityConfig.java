package com.tesco.pma.bpm.camunda.configuration;

import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class CamundaSecurityConfig {

    private static final String AUTHENTICATION_PROVIDER_PARAM = "authentication-provider";

    private static final int WEB_APPS_FILTER_PRECEDENCE = 114;
    private static final String[] WEB_APPS_URL_PATTERNS = {"/camunda/app/*", "/camunda/api/*"};

    private static final int REST_API_FILTER_PRECEDENCE = 115;
    private static final String[] REST_API_URL_PATTERNS = {"/engine-rest/*"};

    /**
     * Register the servlet filter for the Web Apps (Tasklist, Cockpit, Admin)
     *
     * @return Filter registration bean
     */
    @Bean
    @ConditionalOnProperty(name = "tesco.application.security.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<ContainerBasedAuthenticationFilter> camundaWebAppsAuthFilter() {
        FilterRegistrationBean<ContainerBasedAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setName("camunda-web-apps-auth");
        filterRegistration.setFilter(new ContainerBasedAuthenticationFilter());
        filterRegistration.setInitParameters(Collections.singletonMap(AUTHENTICATION_PROVIDER_PARAM,
                PmaAuthenticationProvider.class.getCanonicalName()));
        /* Make sure the filter is registered after the Spring Security Filter Chain */
        filterRegistration.setOrder(WEB_APPS_FILTER_PRECEDENCE);
        filterRegistration.addUrlPatterns(WEB_APPS_URL_PATTERNS);
        return filterRegistration;
    }

    /**
     * Register the servlet filter for the REST API
     *
     * @return Filter registration bean
     */
    @Bean
    @ConditionalOnProperty(name = "tesco.application.security.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<ProcessEngineAuthenticationFilter> camundaRestApiAuthFilter() {
        FilterRegistrationBean<ProcessEngineAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setName("camunda-rest-api-auth");
        filterRegistration.setFilter(new ProcessEngineAuthenticationFilter());
        filterRegistration.setInitParameters(Collections.singletonMap(AUTHENTICATION_PROVIDER_PARAM,
                PmaAuthenticationProvider.class.getCanonicalName()));
        /* Make sure the filter is registered after the Spring Security Filter Chain */
        filterRegistration.setOrder(REST_API_FILTER_PRECEDENCE);
        filterRegistration.addUrlPatterns(REST_API_URL_PATTERNS);
        return filterRegistration;
    }

}
