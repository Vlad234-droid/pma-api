package com.tesco.pma.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "tesco.application.healthcheck")
public class HealthCheckConfig {

    private final List<ConfigurableHealthIndicator> healthIndicators = new LinkedList<>();

    private List<HealthIndicatorProperties> indicators;

    public List<HealthIndicatorProperties> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<HealthIndicatorProperties> indicators) {
        this.indicators = indicators;
    }

    @Bean(name = "healthIndicators")
    public List<ConfigurableHealthIndicator> healthIndicators(ApplicationContext applicationContext) {
        if (healthIndicators.isEmpty()) {
            for (HealthIndicatorProperties indicatorProperties : getIndicators()) {
                var configurableHealthIndicator = applicationContext.getBean(indicatorProperties.getBeanName(),
                        ConfigurableHealthIndicator.class);
                configurableHealthIndicator.setHealthIndicatorProperties(indicatorProperties);

                healthIndicators.add(configurableHealthIndicator);
            }
        }
        return healthIndicators;
    }
}