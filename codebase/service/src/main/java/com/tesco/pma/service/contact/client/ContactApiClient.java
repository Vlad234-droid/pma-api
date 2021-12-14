package com.tesco.pma.service.contact.client;

import com.tesco.pma.contact.api.Message;

/**
 * Contact Api client interface.
 */

public interface ContactApiClient {

    void sendNotification(Message message, String templateId);

}
