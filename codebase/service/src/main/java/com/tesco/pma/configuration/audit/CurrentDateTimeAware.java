package com.tesco.pma.configuration.audit;

import java.time.Instant;

/**
 * Implementation of {@link DateTimeAware} that returns current datetime: {@link java.time.Instant#now()}.
 */
public class CurrentDateTimeAware implements DateTimeAware<Instant> {

    @Override
    public Instant getDateTime() {
        return Instant.now();
    }
}
