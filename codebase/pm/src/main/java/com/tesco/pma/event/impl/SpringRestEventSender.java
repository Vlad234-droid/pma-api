package com.tesco.pma.event.impl;

import java.util.Collection;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSender;
import com.tesco.pma.event.EventSendingException;
import lombok.extern.slf4j.Slf4j;

/**
 * Send event to URL - target, method /event/handle
 */
@Slf4j
@Component
public class SpringRestEventSender implements EventSender {

    private static final String EVENT_HANDLE_METHOD = "/event/handle";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendEvents(Collection<Event> events, String target) {
        try {
            for (Event event : events) {
                try {
                    HttpEntity<Event> requestBody = new HttpEntity<>(event);
                    ResponseEntity<Event> result = restTemplate.postForEntity(target + EVENT_HANDLE_METHOD, requestBody, Event.class);
                    log.info("Event: {} was send to '{}'", event, target + EVENT_HANDLE_METHOD);
                    if (result.getStatusCode() != HttpStatus.OK) {
                        log.info("Sending event " + event.getEventName() + result.toString());
                        throw new EventSendingException("Sending event " + event.getEventName() + ", to" + target + EVENT_HANDLE_METHOD
                                + ", received: " + result.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("Event: {} can't be sent to '{}'", event, target);
                    throw e;
                }
            }
        } catch (Exception e) {
            log.error("Exception occurs during sending event: {}", events, e);
            throw new EventSendingException(e);
        }
    }

    @Override
    public void registerEvents(Collection<Event> events, String target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void txSendEvents(Collection<Event> events, String target) {
        throw new UnsupportedOperationException();
    }
}
