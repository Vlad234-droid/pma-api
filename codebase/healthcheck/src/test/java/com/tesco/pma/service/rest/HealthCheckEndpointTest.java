package com.tesco.pma.service.rest;

import com.tesco.pma.TestConfig;
import com.tesco.pma.service.LivenessHealthIndicator;
import com.tesco.pma.service.OverallHealthIndicator;
import com.tesco.pma.service.ReadinessHealthIndicator;
import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.HealthStatus;
import com.tesco.pma.healthcheck.OverallHealth;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.tesco.pma.exception.ErrorCodes.DB_CONNECTION_ERROR;
import static com.tesco.pma.healthcheck.DependencyType.COMPONENT;
import static com.tesco.pma.healthcheck.DependencyType.DEPENDENCY;
import static com.tesco.pma.healthcheck.HealthStatus.FAIL;
import static com.tesco.pma.healthcheck.HealthStatus.OK;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthCheckEndpoint.class)
@WithMockUser(username = HealthCheckEndpointTest.MOCK_CREATOR_ID)
@ContextConfiguration(classes = TestConfig.class)
class HealthCheckEndpointTest extends AbstractEndpointTest {

    static final String MOCK_CREATOR_ID = "MockCreatorId";

    private static final String STATUS_TEMPLATE = "/_status";
    private static final String HEALTH_CHECK_TEMPLATE = "/_healthcheck";
    private static final String READY_TEMPLATE = "/_ready";
    private static final String HC_TEMPLATE = "/hc";
    private static final String WORKING_TEMPLATE = "/_working";
    private static final String LIVE_TEMPLATE = "/live";

    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final Instant CHECKED_DATE = Instant.parse("2021-06-24T15:36:48.433Z");
    private static final String STATUS_OK_RESPONSE_JSON_FILE_NAME = "status_ok_response.json";
    private static final String STATUS_FAIL_RESPONSE_JSON_FILE_NAME = "status_fail_response.json";

    @MockBean
    private OverallHealthIndicator overallHealthIndicator;

    @MockBean
    private ReadinessHealthIndicator readinessHealthIndicator;

    @MockBean
    private LivenessHealthIndicator livenessHealthIndicator;

    @ParameterizedTest
    @MethodSource("provideArgsForGetStatus")
    void getStatus(HealthStatus healthStatus, String expectedFileNameWithResponse) throws Exception {
        var health = new Health();
        health.setStatus(healthStatus);
        health.setVersion(VERSION);
        when(overallHealthIndicator.health()).thenReturn(health);

        var result = performGet(status().isOk(), STATUS_TEMPLATE);

        assertResponseContent(result.getResponse(), expectedFileNameWithResponse);
    }

    private static Stream<Arguments> provideArgsForGetStatus() {
        return Stream.of(
                Arguments.of(OK, STATUS_OK_RESPONSE_JSON_FILE_NAME),
                Arguments.of(FAIL, STATUS_FAIL_RESPONSE_JSON_FILE_NAME)
        );
    }

    private static Stream<Arguments> provideArgsForGetWorkingIfUnauthorized() {
        return Stream.of(
                Arguments.of(OK, STATUS_OK_RESPONSE_JSON_FILE_NAME, READY_TEMPLATE),
                Arguments.of(FAIL, STATUS_FAIL_RESPONSE_JSON_FILE_NAME, READY_TEMPLATE),
                Arguments.of(OK, STATUS_OK_RESPONSE_JSON_FILE_NAME, HC_TEMPLATE),
                Arguments.of(FAIL, STATUS_FAIL_RESPONSE_JSON_FILE_NAME, HC_TEMPLATE),
                Arguments.of(OK, STATUS_OK_RESPONSE_JSON_FILE_NAME, WORKING_TEMPLATE),
                Arguments.of(FAIL, STATUS_FAIL_RESPONSE_JSON_FILE_NAME, WORKING_TEMPLATE),
                Arguments.of(OK, STATUS_OK_RESPONSE_JSON_FILE_NAME, LIVE_TEMPLATE),
                Arguments.of(FAIL, STATUS_FAIL_RESPONSE_JSON_FILE_NAME, LIVE_TEMPLATE)
        );
    }

    @Test
    void cannotGetStatusIfUnauthorized() throws Exception {
        performGetWith(anonymous(), status().isUnauthorized(), MediaType.APPLICATION_JSON, STATUS_TEMPLATE);

        verifyNoInteractions(overallHealthIndicator);
    }


    @ParameterizedTest
    @MethodSource("provideArgsForGetWorkingIfUnauthorized")
    void getWorkingSuccessIfUnauthorized(HealthStatus healthStatus, String expectedFileNameWithResponse,
                                         String urlTemplate) throws Exception {
        var health = new Health();
        health.setStatus(healthStatus);
        health.setVersion(VERSION);
        when(readinessHealthIndicator.health()).thenReturn(health);
        when(livenessHealthIndicator.health()).thenReturn(health);

        var result = performGetWith(anonymous(), status().isOk(), MediaType.APPLICATION_JSON, urlTemplate);

        assertResponseContent(result.getResponse(), expectedFileNameWithResponse);
    }

    @Test
    void getOverallHealthCheckWithOkStatus() throws Exception {
        when(overallHealthIndicator.overallHealth()).thenReturn(getOkOverallHealth());

        var result = performGet(status().isOk(), HEALTH_CHECK_TEMPLATE);

        assertResponseContent(result.getResponse(), "healthcheck_ok_response.json");
    }

    @Test
    void getOverallHealthCheckWithFailedStatus() throws Exception {
        when(overallHealthIndicator.overallHealth()).thenReturn(getFailedOverallHealth());

        var result = performGet(status().isOk(), HEALTH_CHECK_TEMPLATE);

        assertResponseContent(result.getResponse(), "healthcheck_fail_response.json");
    }

    @Test
    void cannotGetOverallHealthCheckIfUnauthorized() throws Exception {
        performGetWith(anonymous(), status().isUnauthorized(), MediaType.APPLICATION_JSON, HEALTH_CHECK_TEMPLATE);

        verifyNoInteractions(overallHealthIndicator);
    }

    private OverallHealth getOkOverallHealth() {
        var okHealth = new OverallHealth();
        okHealth.setStatus(OK);
        okHealth.setVersion(VERSION);
        var components = List.of(okDbIndicator(), okColleagueApiIndicator(), okIdentityApiIndicator());
        okHealth.setComponent(components);

        return okHealth;
    }

    private OverallHealth getFailedOverallHealth() {
        var okHealth = new OverallHealth();
        okHealth.setStatus(FAIL);
        okHealth.setVersion(VERSION);
        var components = List.of(failedDbIndicator(), okColleagueApiIndicator(), okIdentityApiIndicator());
        okHealth.setComponent(components);

        return okHealth;
    }

    private Health okDbIndicator() {
        return new Health("com.tesco.pma.datasource", COMPONENT, "PostgreSQL database",
                OK, "42.2.25", CHECKED_DATE, null);
    }

    private Health failedDbIndicator() {
        return new Health("com.tesco.pma.datasource", COMPONENT, "PostgreSQL database",
                FAIL, "42.2.25", CHECKED_DATE, Map.of("type", DB_CONNECTION_ERROR,
                "body", "Failed to obtain database connection: localhost:5432"));
    }

    private Health okColleagueApiIndicator() {
        return new Health("com.tesco.capi", DEPENDENCY, "Colleague API Platform",
                OK, null, CHECKED_DATE, null);

    }

    private Health okIdentityApiIndicator() {
        return new Health("com.tesco.identity", DEPENDENCY, "Identity API Platform",
                OK, null, CHECKED_DATE, null);
    }
}
