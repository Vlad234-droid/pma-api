package com.tesco.pma.configuration.audit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;

/**
 * Configuration for auditing functionality.
 */
@Configuration
public class AuditingConfiguration {

    @Value("${tesco.application.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return new SpringSecurityAuditorAware(securityEnabled);
    }

    @Bean
    public DateTimeAware<Instant> dateTimeAware() {
        return new CurrentDateTimeAware();
    }
}
