package com.tesco.pma.configuration.audit;

import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Implementation of {@link AuditorAware} that tries to obtain current auditor from current spring security authentication name.
 *
 * <p>In case current {@link org.springframework.security.core.Authentication} is null fallback to 'unknownAuditor' property
 * witch defaults to 'unknown'.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    private static final String DEFAULT_UNKNOWN_AUDITOR = "unknown";

    @Setter
    private String unknownAuditor = DEFAULT_UNKNOWN_AUDITOR;

    @Override
    public String getCurrentAuditor() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return unknownAuditor;
    }
}
