package com.tesco.pma.event.impl;

import java.util.Collection;
import java.util.Map;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Send event to URL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringRestEventSender implements EventSender {

    @Value("${tesco.application.external-endpoints.events-api.base-url}")
    private String eventHandleApiConfig;

    private final RestTemplate restTemplate;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public void sendEvents(@NonNull Collection<Event> events, String target) {
        var url = target == null ? eventHandleApiConfig : target;

        for (Event event : events) {
            try {
                HttpEntity<Event> requestBody = new HttpEntity<>(event);
                ResponseEntity<Event> result = restTemplate.postForEntity(url, requestBody, Event.class);

                if (result.getStatusCode() != HttpStatus.OK) {

                    log.warn(messageSourceAccessor.getMessage(ErrorCodes.EVENT_SENDING_ERROR,
                            Map.of("event", event, "url", url)));
                }

                log.info("Event: {} was send to '{}'", event, eventHandleApiConfig);

            } catch (Exception e) {
                log.error(messageSourceAccessor.getMessage(ErrorCodes.EVENT_SENDING_ERROR,
                        Map.of("event", event, "url", url)), e);
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
