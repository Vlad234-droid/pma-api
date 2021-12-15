package com.tesco.pma.event.service;

import com.tesco.pma.event.Event;

import java.util.Collection;
import java.util.Collections;

public interface EventSender {

    boolean DEFAULT_THROW = false;

    /**
     * Send events to target destination
     * @param events events
     * @param target destination url
     * @param isThrow should exception be thrown
     * @throws com.tesco.pma.event.exception.EventSendingException
     */
    default void sendEvents(Collection<? extends Event> events, String target, boolean isThrow) {
        events.forEach(e -> sendEvent(e, target, isThrow));
    }

    /**
     * Send events to default target destination
     * @param events events
     * @throws com.tesco.pma.event.exception.EventSendingException
     */
    default void sendEvents(Collection<? extends Event> events) {
        sendEvents(events, null, DEFAULT_THROW);
    }

    /**
     * Send event to target destination
     * @param event an event
     * @param target destination url
     * @param isThrow should exception be thrown
     * @throws com.tesco.pma.event.exception.EventSendingException
     */
    void sendEvent(Event event, String target, boolean isThrow);

    /**
     * Send event to default target destination
     * @param event an event
     */
    default void sendEvent(Event event) {
        sendEvent(event, null, DEFAULT_THROW);
    }

    /**
     * Register events for target destination
     * @param events events
     * @param target destination
     */
    void registerEvents(Collection<Event> events, String target);

    /**
     * Register event for target destination
     * @param event an event
     * @param target destination
     */
    default void register(Event event, String target) {
        registerEvents(Collections.singletonList(event), target);
    }

    /**
     * Send registered events for target destination
     * @param events events
     * @param target destination
     */
    void txSendEvents(Collection<Event> events, String target);

    /**
     * Send registered event for target destination
     * @param event an event
     * @param target destination
     */
    default void txSend(Event event, String target) {
        txSendEvents(Collections.singletonList(event), target);
    }
}
