package com.tesco.pma.service.colleague.inbox.client;

import com.tesco.pma.colleague.inbox.CreateMessageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RestTemplateBasedColleagueInboxApiClient implements ColleagueInboxApiClient {

    @Value("${tesco.application.external-endpoints.colleague-inbox-api.base-url}")
    private String contactMessagingUrl;

    private final RestTemplate restTemplate;

    @Override
    public void sendNotification(CreateMessageRequestDto message) {
        restTemplate.postForLocation(contactMessagingUrl, message);
    }

}
