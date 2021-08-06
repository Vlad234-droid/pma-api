package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthIndicator;
import com.tesco.pma.healthcheck.HealthStatus;
import com.tesco.pma.healthcheck.OverallHealth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.healthcheck.HealthStatus.FAIL;
import static com.tesco.pma.healthcheck.HealthStatus.OK;

/**
 * Health Indicator that helps to diagnose the state of API domain and all its components and dependencies
 * Ex. of _healthcheck endpoint response:
 * {
 *   "status": "Ok",
 *   "version": "1.0.0-SNAPSHOT",
 *   "component": [
 *     {
 *       "name": "defaultPostgreSQLDB",
 *       "type": "COMPONENT",
 *       "description": "Default database",
 *       "status": "Ok",
 *       "version": "12.5 (Debian 12.5-1.pgdg100+1)",
 *       "checked": "2021-07-01T11:15:02.755Z"
 *     },
 *     {
 *       "name": "identityAPI",
 *       "type": "DEPENDENCY",
 *       "description": "Identity API",
 *       "status": "Ok",
 *       "checked": "2021-07-01T11:15:02.755Z"
 *     },
 *     {
 *       "name": "colleagueFactsAPI",
 *       "type": "DEPENDENCY",
 *       "description": "Colleague Facts API",
 *       "status": "Ok",
 *       "checked": "2021-07-01T11:15:02.757Z"
 *     }
 *   ]
 * }
 * Ex. of _status endpoint response:
 * {
 *   "status": "Ok",
 *   "version": "1.0.0-SNAPSHOT"
 * }
 *
 * @see <a href='https://tesco.sharepoint.com/sites/Tescopedia/Code.aspx?Id=181'>Tesco healthchecks docs</a>
 */
@Component
public class OverallHealthIndicator implements HealthIndicator {

    private final List<ConfigurableHealthIndicator> healthIndicators;
    private final BuildProperties buildProperties;

    public OverallHealthIndicator(@Qualifier("healthIndicators") @NotEmpty List<ConfigurableHealthIndicator> healthIndicators,
                                  BuildProperties buildProperties) {
        this.healthIndicators = healthIndicators;
        this.buildProperties = buildProperties;
    }

    @Override
    public Health health() {
        var health = new Health();
        health.setStatus(getOverallStatus());
        health.setVersion(retrieveVersion());
        return health;
    }

    public OverallHealth overallHealth() {
        var health = new OverallHealth();
        health.setVersion(retrieveVersion());
        var components = healthIndicators.parallelStream().map(HealthIndicator::health).collect(Collectors.toList());
        health.setComponent(components);
        health.setStatus(getOverallStatus(components));

        return health;
    }

    private String retrieveVersion() {
        var version = buildProperties.getVersion();
        return version == null ? "unknown" : version;
    }

    private HealthStatus getOverallStatus() {
        for (var indicator : healthIndicators) {
            if (OK != indicator.health().getStatus()) {
                return FAIL;
            }
        }
        return OK;
    }

    private HealthStatus getOverallStatus(final List<Health> components) {
        for (var component : components) {
            if (OK != component.getStatus()) {
                return FAIL;
            }
        }
        return OK;
    }
}