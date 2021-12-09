package com.tesco.pma.service.contact.client;

import com.tesco.pma.contact.api.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@Component
public class RestTemplateBasedContactApiClient implements ContactApiClient {

    private final RestTemplate restTemplate;
    private final UriBuilderFactory uriBuilderFactory;
    private final String notificationsTemplateId;

    public RestTemplateBasedContactApiClient(RestTemplate restTemplate,
                      @Value("${tesco.application.external-endpoints.contact-api.messaging-url}")
                      String contactMessagingUrl,
                      @Value("${tesco.application.external-endpoints.contact-api.notifications-template-id}")
                      String notificationsTemplateId) {

        this.restTemplate = restTemplate;
        this.uriBuilderFactory = new DefaultUriBuilderFactory(contactMessagingUrl);
        this.notificationsTemplateId = notificationsTemplateId;
    }

    @Override
    public void sendNotification(Message message) {
        final var uriBuilder = uriBuilderFactory.builder().path("/{notificationsTemplateId}");
        restTemplate.postForLocation(uriBuilder.build(notificationsTemplateId), message);
    }

}
