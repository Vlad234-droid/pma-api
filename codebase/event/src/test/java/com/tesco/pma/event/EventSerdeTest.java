package com.tesco.pma.event;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventSerdeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final String ROOT = "root";
    private static final String CALLBACK = "callback";
    private static final String CALLBACK_SERVICE_URL = "callbackServiceURL";

    private static final String DATE_KEY = "dateKey";
    private static final Date DATE_PROPERTY = new Date();
    private static final String LONG_KEY = "longKey";
    private static final ArrayList<Serializable> LIST = new ArrayList<>(); //NOPMD
    private static final String LIST_KEY = "listKey";

    @Test // NOPMD
    void test() throws IOException { //NOPMD
        EventSupport event = new EventSupport(ROOT);
        event.setCallbackServiceURL(CALLBACK_SERVICE_URL);
        event.putProperty(DATE_KEY, DATE_PROPERTY);
        event.putProperty(LONG_KEY, 32L);
        event.setCallbackEvent(new EventSupport(CALLBACK));
        EventSupport callbackEvent = (EventSupport) event.getCallbackEvent();
        callbackEvent.setEventPriority(EventPriority.HIGH);
        LIST.add(1L);
        LIST.add("test");
        LIST.add(15);
        ArrayList<String> serializable = new ArrayList<>();
        serializable.add("test");
        LIST.add(serializable);
        LIST.add(Double.parseDouble("34.23"));
        callbackEvent.putProperty(LIST_KEY, LIST);

        String content = MAPPER.writeValueAsString(event);
        System.out.println(content); //NOPMD
        Event actual = MAPPER.readValue(content, Event.class);
        assertNotNull(actual);
        assertEquals(event.getEventId(), actual.getEventId());
        assertEquals(ROOT, actual.getEventName());
        assertEquals(CALLBACK_SERVICE_URL, actual.getCallbackServiceURL());
        assertEquals(EventPriority.NORMAL, actual.getEventPriority());
        assertProperty(event, actual, DATE_KEY);
        assertProperty(event, actual, LONG_KEY);
        Event actualCallbackEvent = actual.getCallbackEvent();
        assertNotNull(actualCallbackEvent);
        assertEquals(callbackEvent.getEventId(), actualCallbackEvent.getEventId());
        assertEquals(CALLBACK, actualCallbackEvent.getEventName());
        assertEquals(callbackEvent.getEventPriority(), actualCallbackEvent.getEventPriority());
        assertProperty(callbackEvent, actualCallbackEvent, LIST_KEY);
    }

    private void assertProperty(Event event, Event actual, String key) {
        assertEquals(event.getEventProperties().get(key), actual.getEventProperties().get(key));
    }

}
