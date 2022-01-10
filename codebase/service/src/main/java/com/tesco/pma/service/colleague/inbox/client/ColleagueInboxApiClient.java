package com.tesco.pma.service.colleague.inbox.client;


import com.tesco.colleague.inbox.web.dto.CreateMessageRequestDto;

/**
 * Colleague Inbox Api client interface.
 */

public interface ColleagueInboxApiClient {

    void sendNotification(CreateMessageRequestDto message);

}
