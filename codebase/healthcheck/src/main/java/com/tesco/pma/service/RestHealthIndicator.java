package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.configuration.HealthIndicatorProperties;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;

import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;
import static com.tesco.pma.healthcheck.DependencyType.DEPENDENCY;
import static java.time.Instant.now;

/**
 * Health Indicator that helps to diagnose our external dependency state (ex. Identity API, Colleague API)
 * Ex. of response:
 * {
 *   "name": "identityAPI",
 *   "type": "DEPENDENCY",
 *   "description": "Identity API",
 *   "status": "Ok",
 *   "checked": "2021-07-01T11:15:02.755Z"
 * }
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RestHealthIndicator implements ConfigurableHealthIndicator {

    private static final String API_NAME = "apiName";

    private final RestTemplate restTemplate;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private HealthIndicatorProperties properties;

    public RestHealthIndicator(RestTemplate restTemplate,
                               NamedMessageSourceAccessor messageSourceAccessor) {
        this.restTemplate = restTemplate;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Override
    public HealthIndicatorProperties getHealthIndicatorProperties() {
        return properties;
    }

    @Override
    public void setHealthIndicatorProperties(HealthIndicatorProperties indicatorProperties) {
        this.properties = indicatorProperties;
    }

    @Override
    public Health health() {
        var health = new Health();
        health.setName(properties.getName());
        health.setDescription(properties.getDescription());
        health.setType(DEPENDENCY);
        health.setChecked(now());

        try {
            var healthResponse = restTemplate.getForEntity(properties.getUrl(), HttpEntity.class);
            if (HttpStatus.OK == healthResponse.getStatusCode()) {
                health.setStatus(HealthStatus.OK);
            } else {
                finalizeFailed(health);
            }
        } catch (RestClientException e) {
            var errorDetails = finalizeFailed(health);
            log.error(errorDetails, e);
        }

        return health;
    }

    private String finalizeFailed(Health health) {
        health.setStatus(HealthStatus.FAIL);
        var errorDetails = messageSourceAccessor.getMessage(EXTERNAL_API_CONNECTION_ERROR, Map.of(API_NAME, properties.getName()));
        health.setError(Map.of("type", EXTERNAL_API_CONNECTION_ERROR, "body", errorDetails));

        return errorDetails;
    }
}