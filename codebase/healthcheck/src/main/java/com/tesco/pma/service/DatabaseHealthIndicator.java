package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.configuration.HealthIndicatorProperties;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;

import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.DB_CONNECTION_ERROR;
import static com.tesco.pma.healthcheck.DependencyType.COMPONENT;
import static java.time.Instant.now;

/**
 * Health Indicator that helps to diagnose database connection state
 * Ex. of response:
 * {
 *   "name": "defaultPostgreSQLDB",
 *   "type": "COMPONENT",
 *   "description": "Default database",
 *   "status": "Ok",
 *   "version": "12.5 (Debian 12.5-1.pgdg100+1)",
 *   "checked": "2021-07-01T11:15:02.755Z"
 * }
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatabaseHealthIndicator implements ConfigurableHealthIndicator {

    private final ApplicationContext applicationContext;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private HealthIndicatorProperties properties;

    public DatabaseHealthIndicator(ApplicationContext applicationContext, NamedMessageSourceAccessor messageSourceAccessor) {
        this.applicationContext = applicationContext;
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
        health.setType(COMPONENT);
        health.setChecked(now());

        var dataSource = applicationContext.getBean(properties.getReference(), DataSource.class);

        var dataSourceHealthIndicator = new DataSourceHealthIndicator(dataSource, properties.getValidationQuery());
        var dataSourceHealth = dataSourceHealthIndicator.getHealth(true);

        if (Status.UP == dataSourceHealth.getStatus()) {
            health.setStatus(HealthStatus.OK);
            health.setVersion((String) dataSourceHealth.getDetails().get("result"));
        } else {
            health.setStatus(HealthStatus.FAIL);
            var errorDetails = messageSourceAccessor.getMessage(DB_CONNECTION_ERROR,
                    Map.of("name", properties.getName(), "reference", properties.getReference() , "url", properties.getUrl()));
            health.setError(Map.of("type", DB_CONNECTION_ERROR, "body", errorDetails));
        }
        return health;
    }
}