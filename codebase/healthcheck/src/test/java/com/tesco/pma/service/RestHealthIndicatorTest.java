package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.TestConfig;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.healthcheck.DependencyType;
import com.tesco.pma.healthcheck.HealthStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
class RestHealthIndicatorTest {

    private static final String BODY = "body";
    private static final String API_NAME = "apiName";

    @Autowired
    @Qualifier("healthIndicators")
    private List<ConfigurableHealthIndicator> healthIndicators;

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void success() {
        // Arrange
        var apiHealthIndicator = healthIndicators.get(2);

        when(restTemplate.getForEntity("https://test.com/identity/_status", HttpEntity.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        setField(apiHealthIndicator, "restTemplate", restTemplate);

        // Act
        var health = apiHealthIndicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(HealthStatus.OK, health.getStatus());
        assertEquals("identityAPI", health.getName());
        assertEquals("Identity API", health.getDescription());
        assertEquals(DependencyType.DEPENDENCY, health.getType());
        assertNotNull(health.getChecked());
        assertNull(health.getError());
    }

    @Test
    void unsuccess() {
        // Arrange
        var apiHealthIndicator = healthIndicators.get(3);
        when(restTemplate.getForEntity("https://test.com/colleague/_status", HttpEntity.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));
        setField(apiHealthIndicator, "restTemplate", restTemplate);

        // Act
        var health = apiHealthIndicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(HealthStatus.FAIL, health.getStatus());
        assertEquals("colleagueFactsAPI", health.getName());
        assertEquals("Colleague Facts API", health.getDescription());
        assertEquals(DependencyType.DEPENDENCY, health.getType());
        assertNotNull(health.getChecked());
        assertNotNull(health.getError());
        assertEquals(EXTERNAL_API_CONNECTION_ERROR, health.getError().get("type"));
        assertNotNull(health.getError().get(BODY));
        assertEquals(messageSourceAccessor.getMessage(EXTERNAL_API_CONNECTION_ERROR, Map.of(API_NAME, health.getName())),
                health.getError().get(BODY));
    }
}