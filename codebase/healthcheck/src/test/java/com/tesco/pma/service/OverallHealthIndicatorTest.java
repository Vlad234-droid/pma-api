package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.healthcheck.Health;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.DB_CONNECTION_ERROR;
import static com.tesco.pma.healthcheck.DependencyType.COMPONENT;
import static com.tesco.pma.healthcheck.DependencyType.DEPENDENCY;
import static com.tesco.pma.healthcheck.HealthStatus.FAIL;
import static com.tesco.pma.healthcheck.HealthStatus.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OverallHealthIndicatorTest {

    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final Instant CHECKED_DATE = Instant.parse("2021-06-24T15:36:48.433Z");

    private final DatabaseHealthIndicator databaseHealthIndicator = Mockito.mock(DatabaseHealthIndicator.class);

    private final RestHealthIndicator identityApiHealthIndicator = Mockito.mock(RestHealthIndicator.class);

    private final RestHealthIndicator colleagueApiHealthIndicator = Mockito.mock(RestHealthIndicator.class);

    private final List<ConfigurableHealthIndicator> healthIndicators =
            List.of(databaseHealthIndicator, identityApiHealthIndicator, colleagueApiHealthIndicator);

    private final BuildProperties buildProperties = Mockito.mock(BuildProperties.class);

    private final OverallHealthIndicator overallHealthIndicator = new OverallHealthIndicator(healthIndicators, buildProperties);

    @BeforeEach
    void init() {
        doReturn(VERSION).when(buildProperties).getVersion();
    }

    @Test
    void successHealth() {
        // Arrange
        when(databaseHealthIndicator.health()).thenReturn(okDbIndicatorHealth());
        when(identityApiHealthIndicator.health()).thenReturn(okIdentityApiIndicator());
        when(colleagueApiHealthIndicator.health()).thenReturn(okColleagueApiIndicatorHealth());

        // Act
        var health = overallHealthIndicator.health();

        // Assert
        assertEquals(OK, health.getStatus());
        assertEquals(VERSION, health.getVersion());

        verify(databaseHealthIndicator).health();
        verify(identityApiHealthIndicator).health();
        verify(colleagueApiHealthIndicator).health();
    }

    @Test
    void successOverallHealth() {
        // Arrange
        when(databaseHealthIndicator.health()).thenReturn(okDbIndicatorHealth());
        when(identityApiHealthIndicator.health()).thenReturn(okIdentityApiIndicator());
        when(colleagueApiHealthIndicator.health()).thenReturn(okColleagueApiIndicatorHealth());

        // Act
        var overallHealth = overallHealthIndicator.overallHealth();

        // Assert
        assertEquals(OK, overallHealth.getStatus());
        assertEquals(VERSION, overallHealth.getVersion());
        assertEquals(healthIndicators.size(), overallHealth.getComponent().size());
        assertEquals(okDbIndicatorHealth(), overallHealth.getComponent().get(0));
        assertEquals(okIdentityApiIndicator(), overallHealth.getComponent().get(1));
        assertEquals(okColleagueApiIndicatorHealth(), overallHealth.getComponent().get(2));

        verify(databaseHealthIndicator).health();
        verify(identityApiHealthIndicator).health();
        verify(colleagueApiHealthIndicator).health();
    }

    @Test
    void unsuccessHealth() {
        // Arrange
        when(databaseHealthIndicator.health()).thenReturn(failedDbIndicatorHealth());
        when(identityApiHealthIndicator.health()).thenReturn(okIdentityApiIndicator());
        when(colleagueApiHealthIndicator.health()).thenReturn(okColleagueApiIndicatorHealth());

        // Act
        var health = overallHealthIndicator.health();

        // Assert
        assertEquals(FAIL, health.getStatus());
        assertEquals(VERSION, health.getVersion());
    }

    @Test
    void unsuccessOverallHealth() {
        // Arrange
        when(databaseHealthIndicator.health()).thenReturn(failedDbIndicatorHealth());
        when(identityApiHealthIndicator.health()).thenReturn(okIdentityApiIndicator());
        when(colleagueApiHealthIndicator.health()).thenReturn(okColleagueApiIndicatorHealth());

        // Act
        var overallHealth = overallHealthIndicator.overallHealth();

        // Assert
        assertEquals(FAIL, overallHealth.getStatus());
        assertEquals(VERSION, overallHealth.getVersion());
        assertEquals(healthIndicators.size(), overallHealth.getComponent().size());
        assertEquals(failedDbIndicatorHealth(), overallHealth.getComponent().get(0));
        assertEquals(okIdentityApiIndicator(), overallHealth.getComponent().get(1));
        assertEquals(okColleagueApiIndicatorHealth(), overallHealth.getComponent().get(2));
    }

    private Health okDbIndicatorHealth() {
        return new Health("defaultPostgreSQLDB", COMPONENT, "Default database",
                OK, "12.5 (Debian 12.5-1.pgdg100+1)", CHECKED_DATE, null);
    }

    private Health failedDbIndicatorHealth() {
        return new Health("defaultPostgreSQLDB", COMPONENT, "Default database",
                FAIL, "12.5 (Debian 12.5-1.pgdg100+1)", CHECKED_DATE, Map.of("type", DB_CONNECTION_ERROR,
                "body", "Failed to obtain database connection: localhost:5432"));
    }

    private Health okColleagueApiIndicatorHealth() {
        return new Health("colleagueFactsAPI", DEPENDENCY, "Colleague Facts API",
                OK, null, CHECKED_DATE, null);

    }

    private Health okIdentityApiIndicator() {
        return new Health("identityAPI", DEPENDENCY, "Identity API",
                OK, null, CHECKED_DATE, null);
    }
}