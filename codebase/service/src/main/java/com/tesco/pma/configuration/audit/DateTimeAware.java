package com.tesco.pma.configuration.audit;

/**
 * Interface for components that are aware of datetime.
 *
 * @param <T> - type of datetime.
 */
public interface DateTimeAware<T> {
    T getDateTime();
}
