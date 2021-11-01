package com.tesco.pma.configuration.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

/**
 * Configuration for auditing functionality.
 */
@Configuration
public class AuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new SpringSecurityAuditorAware();
    }

    @Bean
    public DateTimeAware<Instant> dateTimeAware() {
        return new CurrentDateTimeAware();
    }
}
