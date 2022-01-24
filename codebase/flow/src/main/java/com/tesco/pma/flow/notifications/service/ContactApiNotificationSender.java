package com.tesco.pma.flow.notifications.service;

import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.contact.api.DestinationType;
import com.tesco.pma.contact.api.Message;
import com.tesco.pma.contact.api.Recipient;
import com.tesco.pma.service.contact.client.ContactApiClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

//@Service //TODO return later
@AllArgsConstructor
public class ContactApiNotificationSender implements SendNotificationService {

    private final ContactApiClient contactApiClient;

    @Override
    public void send(ColleagueProfile colleagueProfile, String templateId, Map<String, String> placeholders) {

        contactApiClient.sendNotification(
                getMessage(colleagueProfile.getColleague().getColleagueUUID(), placeholders), templateId);

    }

    protected Message getMessage(UUID colleagueId, Map<String, String> placeholders) {
        var message = new Message();
        var recipient = new Recipient();
        recipient.setUuid(colleagueId);
        recipient.setDestination(DestinationType.EMAIL_CC);
        message.setRecipients(List.of(recipient));
        message.setData(placeholders);
        return message;
    }
}
