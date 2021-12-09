package com.tesco.pma.event.impl;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.exception.ErrorCodes;
import com.tesco.pma.event.exception.EventSendingException;
import com.tesco.pma.event.service.EventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;

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
    public void sendEvent(Event event, String target, boolean isThrow) {
        var url = getUrl(target);
        try {
            HttpEntity<Event> requestBody = new HttpEntity<>(event);
            ResponseEntity<Event> result = restTemplate.postForEntity(url, requestBody, Event.class);

            if (result.getStatusCode() != HttpStatus.OK) {
                var message = messageSourceAccessor.getMessage(ErrorCodes.EVENT_SENDING_ERROR,
                        Map.of("event", event, "url", url));

                if (result.getStatusCode().is5xxServerError()) {
                    throw new HttpServerErrorException(result.getStatusCode(), message);
                }

                if (result.getStatusCode().is4xxClientError()) {
                    throw new HttpClientErrorException(result.getStatusCode(), message);
                }
            }

            log.info("Event: {} was sent to '{}'", event, eventHandleApiConfig);

        } catch (Exception e) {

            if (isThrow) {
                throw new EventSendingException(ErrorCodes.EVENT_SENDING_ERROR.getCode(),
                        messageSourceAccessor.getMessage(ErrorCodes.EVENT_SENDING_ERROR,
                                Map.of("event", event, "url", url)), null, e);
            } else {
                log.warn(messageSourceAccessor.getMessage(ErrorCodes.EVENT_SENDING_ERROR,
                        Map.of("event", event, "url", url)), e);
            }
        }
    }

    private String getUrl(String target) {
        return target == null ? eventHandleApiConfig : target;
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
