package com.tesco.pma.configuration;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@ComponentScan(basePackages = "com.tesco.pma.configuration")
@Import({JacksonAutoConfiguration.class,
        RestTemplateAutoConfiguration.class
})
class TestConfig {
}
