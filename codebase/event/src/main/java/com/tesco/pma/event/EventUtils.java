package com.tesco.pma.event;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EventUtils {
    private EventUtils() {
    }

    /**
     * Retrieves a collection of long values (e.g. identifiers) from an event property:
     * if the property is a collection then for the each value
     *      if the value is a number convert to long and add to the returning collection
     *      else if the value is a string try to parse a long value and add to the returning collection
     *      otherwise writes warnings to the log
     *
     * @param event an event
     * @param propertyName values property name
     * @return always returns a collection of long values or an empty collection if no one value was converted
     */
    public static Collection<Long> retrieveLongValues(Event event, String propertyName) {
        Object oids = event.getEventProperty(propertyName);
        if (oids instanceof Collection) {
            Collection<Long> ids = new LinkedHashSet<>();
            for (Object o : (Collection) oids) {
                if (o instanceof Number) {
                    ids.add(((Number) o).longValue());
                } else if (o instanceof String) {
                    try {
                        ids.add(Long.valueOf((String) o));
                    } catch (NumberFormatException e) {
                        log.warn("Identifier cannot be casted to Long: {}", o, e);
                    }
                } else {
                    log.warn("Unhandled type: {}", o);
                }
            }
            return ids;
        }
        if (oids == null) {
            log.warn("Event property {} was not found", propertyName);
        } else {
            log.warn("Event property {} is not a collection: {}", propertyName, oids);
        }
        return Collections.emptySet();
    }
}
