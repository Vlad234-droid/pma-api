package com.tesco.pma.service.cep;

import com.tesco.pma.configuration.cep.CEPProperties;
import com.tesco.pma.api.Identified;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.EventPublishException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private final CEPProperties cepProperties;
    private final RestTemplate restTemplate;

    public EventServiceImpl(CEPProperties cepProperties, RestTemplate restTemplate) {
        this.cepProperties = cepProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> void sendEvent(Identified<T> payload) {
        var publish = cepProperties.getPublish();
        var feedId = publish.getEvents().get(payload.getClass().getSimpleName()).getFeedId();

        var eventPayloadId = payload.getId().toString();
        var eventId = UUID.randomUUID().toString();

        var eventRequest = new EventRequest<>(
                new EventMetadata(eventId, feedId, eventPayloadId), payload
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(eventRequest, headers);
        try {
            var response = restTemplate.postForEntity(publish.getUrl(), entity, String.class);
            resolveResponseStatus(eventId, response.getStatusCode(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            resolveResponseStatus(eventId, ex.getStatusCode(), ex.getResponseBodyAsString());
        }
    }

    private void resolveResponseStatus(String eventId, HttpStatus statusCode, String body) {
        switch (statusCode) {
            case OK:
                throw new EventPublishException(ErrorCodes.EVENT_NOT_PUBLISHED.getCode(),
                        String.format("Event %s accepted but not published", eventId));
            case CREATED:
                log.trace("Event {} has been successfully published", eventId);
                break;
            case UNAUTHORIZED:
                throw new EventPublishException(ErrorCodes.USER_NOT_AUTHENTICATED.getCode(), "User is not authenticated");
            case FORBIDDEN:
                throw new EventPublishException(ErrorCodes.USER_NOT_AUTHORIZED.getCode(), "User is not authorized");
            case UNPROCESSABLE_ENTITY:
                throw new EventPublishException(ErrorCodes.EVENT_PAYLOAD_ERROR.getCode(),
                        String.format("Event %s has errors in payload: %s", eventId, body));
            default:
                throw new EventPublishException(ErrorCodes.EVENT_UNEXPECTED_ERROR.getCode(),
                        String.format("Unexpected status code: %s, body: %s", statusCode, body));
        }
    }
}
