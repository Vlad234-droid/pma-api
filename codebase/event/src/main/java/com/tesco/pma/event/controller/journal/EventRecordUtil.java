package com.tesco.pma.event.controller.journal;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;

// TODO revise class usage to reuse central ObjectMapper
public class EventRecordUtil {

    private static final ObjectMapper OM = new ObjectMapper();

    private EventRecordUtil() {
    }

    public static String getEventBody(Event event) throws Exception {
        return OM.writeValueAsString(event);
    }
    
    public static Event createEvent(String eventBody) throws Exception {
        return OM.readValue(eventBody, EventSupport.class);
    }
}
