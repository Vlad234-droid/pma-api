package com.tesco.pma.configuration.audit;

import com.tesco.pma.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.UNAUTHENTICATED;

/**
 * Implementation of {@link AuditorAware} that tries to obtain current auditor from current spring security authentication name.
 */
@AllArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {
    private static final UUID DEFAULT_UNKNOWN_AUDITOR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final boolean securityEnabled;

    @Override
    public UUID getCurrentAuditor() {
        if (securityEnabled) {
            final var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                return UUID.fromString(authentication.getName());
            } else {
                throw new NotFoundException(UNAUTHENTICATED.getCode(), "User is unauthenticated");
            }
        } else {
            return DEFAULT_UNKNOWN_AUDITOR_UUID;
        }
    }
}
