package com.tesco.pma.configuration;

import com.tesco.pma.healthcheck.HealthIndicator;

public interface ConfigurableHealthIndicator extends HealthIndicator {
    HealthIndicatorProperties getHealthIndicatorProperties();

    void setHealthIndicatorProperties(HealthIndicatorProperties indicatorProperties);
}