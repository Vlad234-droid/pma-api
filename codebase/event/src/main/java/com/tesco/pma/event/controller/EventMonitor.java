package com.tesco.pma.event.controller;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;

/**
 * Should be used to implement monitoring logic
 */
public interface EventMonitor {
    void occurred(Event event);

    void start(Event event);

    void end(Event event, EventResponse response);

    void notMapped(Event event);

    void runtimeError(Event event, Exception exception);

    void unexpectedError(Event event, Exception exception);
}
