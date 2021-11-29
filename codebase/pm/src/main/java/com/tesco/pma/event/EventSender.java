package com.tesco.pma.event;

import com.tesco.pma.event.exception.EventSendingException;

import java.util.Collection;
import java.util.Collections;

public interface EventSender {

    /**
     * Send events to target destination
     * @param events events
     * @param target destination url
     * @throws EventSendingException runtime exception
     */
    void sendEvents(Collection<Event> events, String target);

    /**
     * Send events to default target destination
     * @param events events
     * @throws EventSendingException runtime exception
     */
    default void sendEvents(Collection<Event> events) {
        sendEvents(events, null);
    }

    /**
     * Send event to target destination
     * @param event an event
     * @param target destination url
     * @throws EventSendingException runtime exception
     */
    default void send(Event event, String target) {
        sendEvents(Collections.singletonList(event), target);
    }

    /**
     * Send event to default target destination
     * @param event an event
     * @throws EventSendingException runtime exception
     */
    default void send(Event event) {
        sendEvents(Collections.singletonList(event), null);
    }

    /**
     * Register events for target destination
     * @param events events
     * @param target destination
     * @throws EventSendingException runtime exception
     */
    void registerEvents(Collection<Event> events, String target);

    /**
     * Register event for target destination
     * @param event an event
     * @param target destination
     * @throws EventSendingException runtime exception
     */
    default void register(Event event, String target) {
        registerEvents(Collections.singletonList(event), target);
    }

    /**
     * Send registered events for target destination
     * @param events events
     * @param target destination
     * @throws EventSendingException runtime exception
     */
    void txSendEvents(Collection<Event> events, String target);

    /**
     * Send registered event for target destination
     * @param event an event
     * @param target destination
     * @throws EventSendingException runtime exception
     */
    default void txSend(Event event, String target) {
        txSendEvents(Collections.singletonList(event), target);
    }
}
