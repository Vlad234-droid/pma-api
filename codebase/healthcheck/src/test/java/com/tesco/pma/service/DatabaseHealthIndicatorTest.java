package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.TestConfig;
import com.tesco.pma.healthcheck.DependencyType;
import com.tesco.pma.healthcheck.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static com.tesco.pma.exception.ErrorCodes.DB_CONNECTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 06.07.2021 Time: 17:00
 */
@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
@ActiveProfiles("test")
@SpringBootTest(classes = {TestConfig.class, DatabaseHealthIndicatorTest.TestConfig.class})
class DatabaseHealthIndicatorTest {

    private static final String BODY = "body";

    @Autowired
    @Qualifier("healthIndicators")
    List<ConfigurableHealthIndicator> healthIndicators;

    @Profile("test")
    @Configuration
    public static class TestConfig {
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.testdb")
        public DataSource testDataSource() {
            return DataSourceBuilder.create().build();
        }
    }

    @Test
    void success() {
        var testdbIndicator = healthIndicators.get(1);
        var health = testdbIndicator.health();
        assertNotNull(health);
        Assertions.assertEquals(HealthStatus.OK, health.getStatus());
        assertEquals("testDB", health.getName());
        assertEquals("Test database", health.getDescription());
        Assertions.assertEquals(DependencyType.COMPONENT, health.getType());
        assertFalse(org.apache.commons.lang3.StringUtils.isBlank(health.getVersion()));
    }

    @Test
    void unsuccess() {
        var defaultDbIndicator = healthIndicators.get(0);
        var health = defaultDbIndicator.health();
        assertNotNull(health);
        assertEquals(HealthStatus.FAIL, health.getStatus());
        assertNotNull(health.getError());
        assertEquals(DB_CONNECTION_ERROR, health.getError().get("type"));
        assertNotNull(health.getError().get(BODY));
        assertThat((String) health.getError().get(BODY)).contains(defaultDbIndicator.getHealthIndicatorProperties().getName());
        assertThat((String) health.getError().get(BODY)).contains(defaultDbIndicator.getHealthIndicatorProperties().getReference());
        assertThat((String) health.getError().get(BODY))
                .contains(StringUtils.deleteAny(defaultDbIndicator.getHealthIndicatorProperties().getUrl(), "$"));
    }
}