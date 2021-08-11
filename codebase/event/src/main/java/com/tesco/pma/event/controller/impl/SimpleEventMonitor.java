package com.tesco.pma.event.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.controller.EventMonitor;

public class SimpleEventMonitor implements EventMonitor {

    protected static final Logger logger = LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass()); //NOPMD

    private static final String EVENT_OCCURRED_PATTERN = "EVENT_OCCURRED: {}";
    private static final String EVENT_START_PATTERN = "EVENT_START: {}";
    private static final String EVENT_END_PATTERN = "EVENT_END: {}; EVENT_RESPONSE: {}";
    private static final String EVENT_NOT_MAPPED_PATTERN = "EVENT_NOT_MAPPED: {}";
    private static final String EVENT_ERROR_PATTERN = "EVENT_ERROR: {}";
    private static final String EVENT_UNEXPECTED_ERROR_PATTERN = "EVENT_UNEXPECTED_ERROR: {}";

    @Override
    public void occurred(Event event) {
        logger.info(getEventOccurredPattern(event), event2String(event));
    }

    @Override
    public void start(Event event) {
        logger.info(getEventStartPattern(event), event2String(event));
    }

    @Override
    public void end(Event event, EventResponse response) {
        logger.info(getEventEndPattern(event, response), event2String(event), response);
    }

    @Override
    public void notMapped(Event event) {
        logger.warn(getEventNotMappedPattern(event), event2String(event));
    }

    @Override
    public void runtimeError(Event event, Exception exception) {
        logger.error(getEventRuntimeErrorPattern(event, exception), event2String(event), exception);
    }

    @Override
    public void unexpectedError(Event event, Exception exception) {
        logger.error(getEventUnexpectedErrorPattern(event, exception), event2String(event), exception);
    }

    protected String getEventOccurredPattern(Event event) {
        return EVENT_OCCURRED_PATTERN;
    }

    protected String getEventStartPattern(Event event) {
        return EVENT_START_PATTERN;
    }

    protected String getEventEndPattern(Event event, EventResponse response) {
        return EVENT_END_PATTERN;
    }

    protected String getEventNotMappedPattern(Event event) {
        return EVENT_NOT_MAPPED_PATTERN;
    }

    protected String getEventRuntimeErrorPattern(Event event, Exception exception) {
        return EVENT_ERROR_PATTERN;
    }

    protected String getEventUnexpectedErrorPattern(Event event, Exception exception) {
        return EVENT_UNEXPECTED_ERROR_PATTERN;
    }

    protected static String event2String(Event event) {
        return event == null ? "Undefined event" : event.toString();
    }
}
