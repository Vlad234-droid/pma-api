package com.tesco.pma.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.READINESS_STATE_ERROR;
import static com.tesco.pma.healthcheck.HealthStatus.OK;
import static com.tesco.pma.healthcheck.HealthStatus.FAIL;

/**
 * Health Indicator that helps to diagnose if the application is able to accept traffic
 * Ex. of _ready endpoint response:
 * {
 *   "status": "Ok",
 *   "version": "1.0.0-SNAPSHOT"
 * }
 *
 * @see <a href='https://tesco.sharepoint.com/sites/Tescopedia/Code.aspx?Id=181'>Tesco healthchecks docs</a>
 */
@Component
public class ReadinessHealthIndicator implements HealthIndicator {

    private final ApplicationAvailability applicationAvailability;
    private final BuildProperties buildProperties;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public ReadinessHealthIndicator(ApplicationAvailability applicationAvailability, BuildProperties buildProperties,
                                    NamedMessageSourceAccessor messageSourceAccessor) {
        this.applicationAvailability = applicationAvailability;
        this.buildProperties = buildProperties;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Override
    public Health health() {
        var health = new Health();
        health.setVersion(retrieveVersion());
        if (ReadinessState.ACCEPTING_TRAFFIC == applicationAvailability.getReadinessState()) {
            health.setStatus(OK);
        } else {
            health.setStatus(FAIL);
            var errorDetails = messageSourceAccessor.getMessage(READINESS_STATE_ERROR);
            health.setError(Map.of("type", READINESS_STATE_ERROR, "body", errorDetails));
        }
        return health;
    }

    private String retrieveVersion() {
        var version = buildProperties.getVersion();
        return version == null ? "unknown" : version;
    }
}