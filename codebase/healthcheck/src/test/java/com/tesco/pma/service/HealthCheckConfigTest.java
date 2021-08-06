package com.tesco.pma.service;

import com.tesco.pma.configuration.ConfigurableHealthIndicator;
import com.tesco.pma.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 06.07.2021 Time: 11:09
 */
@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
class HealthCheckConfigTest {

    @Autowired
    @Qualifier("healthIndicators")
    List<ConfigurableHealthIndicator> healthIndicators;

    @Test
    void healthIndicators() {
        Assertions.assertNotNull(healthIndicators);
        Assertions.assertEquals(4, healthIndicators.size());
    }
}