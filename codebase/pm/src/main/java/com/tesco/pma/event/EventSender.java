package com.tesco.pma.event;

import java.util.Collection;
import java.util.Collections;

public interface EventSender {

    /**
     * Send events to target destination
     * @param events events
     * @param target destination url
     */
    void sendEvents(Collection<Event> events, String target);

    /**
     * Send events to default target destination
     * @param events events
     */
    default void sendEvents(Collection<Event> events) {
        sendEvents(events, null);
    }

    /**
     * Send event to target destination
     * @param event an event
     * @param target destination url
     */
    void send(Event event, String target);

    /**
     * Send event to default target destination
     * @param event an event
     */
    default void send(Event event) {
        send(event, null);
    }

    /**
     * Send event to default target destination
     * @param event an event
     * @throws com.tesco.pma.event.exception.EventSendingException
     */
    void sendOrThrow(Event event, String target);

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
