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
        var event = new EventSupport(EVENT_NAME);
        var name = SerdeUtils.EventProperties.EVENT_PRIORITY.name();
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty(name, "value1"));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameEventName() {
        var event = new EventSupport(EVENT_NAME);
        var name = SerdeUtils.EventProperties.EVENT_NAME.name();
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty(name, "value2"));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameCallbackEvent() {
        var event = new EventSupport(EVENT_NAME);
        var name = SerdeUtils.EventProperties.CALLBACK_EVENT.name();
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty(name, "value3"));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNameCallbackServiceURL() {
        var event = new EventSupport(EVENT_NAME);
        var name = SerdeUtils.EventProperties.CALLBACK_SERVICE_URL.name();
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty(name, "value4"));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNullName() {
        var event = new EventSupport(EVENT_NAME);
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty("", null));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyEmptyName() {
        var event = new EventSupport(EVENT_NAME);
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty("", null));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidPropertyNullValue() {
        var event = new EventSupport(EVENT_NAME);
        var name = SerdeUtils.EventProperties.CALLBACK_SERVICE_URL.name();
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty(name, null));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testNotValidCollectionPropertyValue() {
        var event = new EventSupport(EVENT_NAME);
        var list = new ArrayList<>();
        list.add(new Object());
        var expected = assertThrows(IllegalArgumentException.class,
                () -> event.putProperty("prop1", list));
        Assertions.assertNotNull(expected);
    }

    @Test
    void testEventCreate() {
        var code = "code";
        var callbackCode = "callbackCode";
        var callbackServiceURL = "callbackServiceURL";
        var serializableKey = "serializableKey";
        Short serializableValue = 1;
        var event = new EventSupport(code);
        event.setCallbackEvent(new EventSupport(callbackCode));
        event.setCallbackServiceURL(callbackServiceURL);
        event.setEventPriority(EventPriority.HIGH);
        event.putProperty(code, callbackCode);
        event.putProperty(serializableKey, serializableValue);

        assertEquals(code, event.getEventName());
        var eventId = event.getEventId();
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
