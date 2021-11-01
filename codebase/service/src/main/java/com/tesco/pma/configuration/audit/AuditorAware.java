package com.tesco.pma.configuration.audit;

/**
 * Interface for components that are aware of the application's current auditor, e.g. current logged user.
 *
 * @param <T> - type of auditor.
 */
public interface AuditorAware<T> {
    T getCurrentAuditor();
}
