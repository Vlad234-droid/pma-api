package com.tesco.pma.event;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventSupportTest {
    private static final String EVENT_NAME = "test";

    @Test
    void testNotValidPropertyNameEventPriority() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty(SerdeUtils.EventProperties.EVENT_PRIORITY.name(), "value1");
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameEventName() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty(SerdeUtils.EventProperties.EVENT_NAME.name(), "value2");
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameCallbackEvent() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty(SerdeUtils.EventProperties.CALLBACK_EVENT.name(), "value3");
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameCallbackServiceURL() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty(SerdeUtils.EventProperties.CALLBACK_SERVICE_URL.name(), "value4");
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNullName() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty("", null);
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyEmptyName() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty("", null);
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNullValue() {
        EventSupport event = new EventSupport(EVENT_NAME);
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty(SerdeUtils.EventProperties.CALLBACK_SERVICE_URL.name(), null);
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidCollectionPropertyValue() {
        EventSupport event = new EventSupport(EVENT_NAME);
        ArrayList<Object> list = new ArrayList<>();
        list.add(new Object());
        Exception expected = assertThrows(IllegalArgumentException.class, () -> {
            event.putProperty("prop1", list);
        });
        Assertions.assertNotNull(expected);
    }

    @Test
    void testEventCreate() {
        String code = "code";
        String callbackCode = "callbackCode";
        String callbackServiceURL = "callbackServiceURL";
        String serializableKey = "serializableKey";
        Short serializableValue = 1;
        EventSupport event = new EventSupport(code);
        event.setCallbackEvent(new EventSupport(callbackCode));
        event.setCallbackServiceURL(callbackServiceURL);
        event.setEventPriority(EventPriority.HIGH);
        event.putProperty(code, callbackCode);
        event.putProperty(serializableKey, serializableValue);

        assertEquals(code, event.getEventName());
        String eventId = event.getEventId();
        assertNotNull(eventId);
        assertEquals(eventId.length(), UUID.randomUUID().toString().replace("-", "").length());
        assertEquals(EventPriority.HIGH, event.getEventPriority());
        assertEquals(callbackServiceURL, event.getCallbackServiceURL());
        assertEquals(callbackCode, event.getEventProperties().get(code));

        assertEquals(serializableValue, event.getEventProperty(serializableKey));

        assertEquals(callbackCode, event.getCallbackEvent().getEventName());
        assertEquals(EventPriority.NORMAL, event.getCallbackEvent().getEventPriority());
    }
}
