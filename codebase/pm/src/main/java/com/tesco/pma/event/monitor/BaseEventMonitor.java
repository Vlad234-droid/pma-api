package com.tesco.pma.event.monitor;

import org.springframework.stereotype.Component;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.controller.impl.SimpleEventMonitor;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@Component
public class BaseEventMonitor extends SimpleEventMonitor {

    public static final String POINT_EVENT_OCCURRED = "EVENT_OCCURRED";
    public static final String POINT_EVENT_PROCESSING = "EVENT_PROCESSING_START";
    public static final String POINT_EVENT_PROCESSED = "EVENT_PROCESSED";
    public static final String POINT_EVENT_PROCESSED_RESULT = "EVENT_PROCESSED_RESULT";
    public static final String POINT_EVENT_NO_ACTION = "EVENT_HAS_NO_ACTION_MAPPED";
    public static final String POINT_EVENT_PROCESSING_RUNTIME_ERROR = "EVENT_PROCESSING_EVENT_RUNTIME_ERROR";
    public static final String POINT_EVENT_PROCESSING_UNEXPECTED_ERROR = "EVENT_PROCESSING_UNEXPECTED_ERROR";

    @Override
    protected String getEventOccurredPattern(Event event) {
        return POINT_EVENT_OCCURRED + ": {}";
    }

    @Override
    protected String getEventStartPattern(Event event) {
        return POINT_EVENT_PROCESSING + ": {}";
    }

    @Override
    protected String getEventEndPattern(Event event, EventResponse response) {
        return POINT_EVENT_PROCESSED + ": {}; EVENT_RESPONSE: {}";
    }

    @Override
    protected String getEventNotMappedPattern(Event event) {
        return POINT_EVENT_NO_ACTION + ": {}";
    }

    @Override
    protected String getEventRuntimeErrorPattern(Event event, Exception exception) {
        return POINT_EVENT_PROCESSING_RUNTIME_ERROR + ": {}";
    }

    @Override
    protected String getEventUnexpectedErrorPattern(Event event, Exception exception) {
        return POINT_EVENT_PROCESSING_UNEXPECTED_ERROR + ": {}";
    }
}
