package com.tesco.pma.event.controller.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventController;
import com.tesco.pma.event.controller.EventException;
import com.tesco.pma.event.controller.EventMapping;
import com.tesco.pma.event.controller.EventMonitor;

class EventControllerTest {
    private static final String BUSINESS_ID = "BUSINESS_ID";

    private EventController eventController;
    private EventMapping mapping;
    private EventMonitor monitor;

    @BeforeEach
    void init() {
        mapping = mock(EventMapping.class);
        monitor = spy(new SimpleEventMonitor());
        eventController = new EventControllerImpl(mapping, monitor);
    }

    @Test
    void testUndefinedEvent() throws EventException {
        Exception expected = Assertions.assertThrows(EventException.class, () -> {
            eventController.processEvent(null);
        });
        assertNotNull(expected.getCause());
        assertEquals(IllegalArgumentException.class, expected.getCause().getClass());
    }

    @Test
    void testUndefinedEventwithResponse() throws EventException {
        Exception expected = Assertions.assertThrows(EventException.class, () -> {
            eventController.processEventReturnResponse(null);
        });
        assertNotNull(expected.getCause());
        assertEquals(IllegalArgumentException.class, expected.getCause().getClass());

    }

    @Test
    void testEventNotMapped() throws EventException {
        String eventName = "notMapped";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);
        eventController.processEvent(event);

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).notMapped(any());
    }

    @Test
    void testEventNotMappedWithResponse() throws EventException {
        String eventName = "notMapped";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);
        EventResponse response = eventController.processEventReturnResponse(event);

        assertNotNull(response);
        assertEquals(eventName, response.getEventName());
        assertEquals(EventResponse.END, response.getResponseName());
        assertNotNull(response.getSource());

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).notMapped(any());
    }

    @Test
    void testEventError() throws EventException {
        String eventName = "error";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);
        
        Action action = mock(Action.class);
        when(action.perform(any())).thenThrow(EventException.class);
        when(mapping.getAction(any())).thenReturn(action);

        Exception expected = Assertions.assertThrows(EventException.class, () -> {
            eventController.processEvent(event);
        });
        Assertions.assertNotNull(expected);

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).start(any());
        verify(monitor, times(1)).runtimeError(any(), any());
    }

    @Test
    void testEventErrorWithResponse() throws EventException {
        String eventName = "error";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);

        Action action = mock(Action.class);
        when(action.perform(any())).thenThrow(EventException.class);
        when(mapping.getAction(any())).thenReturn(action);

        Exception expected = Assertions.assertThrows(EventException.class, () -> {
            eventController.processEventReturnResponse(event);
        });
        Assertions.assertNotNull(expected);

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).start(any());
        verify(monitor, times(1)).runtimeError(any(), any());
    }

    @Test
    void testEventSuccess() throws EventException {
        String eventName = "success";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);
        EventResponse response = new EventResponseSupport(eventName, eventName, event);
        
        Action action = mock(Action.class);
        when(action.perform(any())).thenReturn(response);
        when(mapping.getAction(any())).thenReturn(action);
        eventController.processEvent(event);

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).start(any());
        verify(monitor, times(1)).end(any(), any());
    }

    @Test
    void testEventSuccessWithResponse() throws EventException {
        String eventName = "success";
        EventSupport event = new EventSupport(eventName);
        event.putProperty(BUSINESS_ID, 1);
        EventResponse response = new EventResponseSupport(eventName, eventName, this);
        
        Action action = mock(Action.class);
        when(action.perform(any())).thenReturn(response);
        when(mapping.getAction(any())).thenReturn(action);
        EventResponse actual = eventController.processEventReturnResponse(event);

        assertNotNull(actual);
        assertEquals(response.getEventName(), actual.getEventName());
        assertEquals(response.getResponseName(), response.getResponseName());
        assertEquals(response.getSource(), actual.getSource());

        verify(monitor, times(1)).occurred(any());
        verify(monitor, times(1)).start(any());
        verify(monitor, times(1)).end(any(), any());
    }
}
