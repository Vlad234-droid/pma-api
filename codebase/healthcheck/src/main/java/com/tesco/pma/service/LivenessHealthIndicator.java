package com.tesco.pma.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.LIVENESS_STATE_ERROR;
import static com.tesco.pma.healthcheck.HealthStatus.FAIL;
import static com.tesco.pma.healthcheck.HealthStatus.OK;

/**
 * Health Indicator that helps to diagnose if the application is running with a correct internal state
 * so the API Domain is in a healthy working state
 * Ex. of _working endpoint response:
 * {
 *  "status": "Ok",
 *  "version": "1.0.0-SNAPSHOT"
 * }
 *
 * @see <a href='https://tesco.sharepoint.com/sites/Tescopedia/Code.aspx?Id=181'>Tesco healthchecks docs</a>
 */
@Component
public class LivenessHealthIndicator implements HealthIndicator {

    private final ApplicationAvailability applicationAvailability;
    private final BuildProperties buildProperties;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public LivenessHealthIndicator(ApplicationAvailability applicationAvailability, BuildProperties buildProperties,
                                   NamedMessageSourceAccessor messageSourceAccessor) {
        this.applicationAvailability = applicationAvailability;
        this.buildProperties = buildProperties;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Override
    public Health health() {
        var health = new Health();
        health.setVersion(retrieveVersion());
        if (LivenessState.CORRECT == applicationAvailability.getLivenessState()) {
            health.setStatus(OK);
        } else {
            health.setStatus(FAIL);
            var errorDetails = messageSourceAccessor.getMessage(LIVENESS_STATE_ERROR);
            health.setError(Map.of("type", LIVENESS_STATE_ERROR, "body", errorDetails));
        }
        return health;
    }

    private String retrieveVersion() {
        var version = buildProperties.getVersion();
        return version == null ? "unknown" : version;
    }
}