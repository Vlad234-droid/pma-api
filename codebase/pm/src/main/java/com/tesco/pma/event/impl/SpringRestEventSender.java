package com.tesco.pma.event.impl;

import java.util.Collection;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.exception.ErrorCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSender;
import com.tesco.pma.event.exception.EventSendingException;
import lombok.extern.slf4j.Slf4j;

/**
 * Send event to URL - target, method /event/handle
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringRestEventSender implements EventSender {

    @Value("${tesco.application.external-endpoints.events-api.base-url}")
    private String eventHandleApi;

    private final RestTemplate restTemplate;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public void sendEvents(@NonNull Collection<Event> events) {
        for (Event event : events) {
            try {
                HttpEntity<Event> requestBody = new HttpEntity<>(event);
                ResponseEntity<Event> result = restTemplate.postForEntity(eventHandleApi, requestBody, Event.class);

                log.info("Event: {} was send to '{}'", event, eventHandleApi);

                if (result.getStatusCode() != HttpStatus.OK) {

                    throw new EventSendingException(ErrorCodes.EVENT_SENDING_ERROR.getCode(),
                            messageSourceAccessor.getMessage(eventHandleApi));
                }

            } catch (Exception e) {
                log.error("Error while sending event {} to {}", event, eventHandleApi, e);
            }
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
