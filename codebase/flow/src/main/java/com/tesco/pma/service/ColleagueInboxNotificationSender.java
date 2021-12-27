package com.tesco.pma.service;

import com.tesco.pma.colleague.inbox.CreateMessageRequestDto;
import com.tesco.pma.colleague.inbox.MessageCategory;
import com.tesco.pma.colleague.inbox.RecipientDto;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ColleagueInboxNotificationSender implements SendNotificationService {

    private final ColleagueInboxApiClient colleagueInboxApiClient;

    @Override
    public void send(ColleagueProfile colleagueProfile, String templateId, Map<String, String> placeholders) {
        colleagueInboxApiClient.sendNotification(getMessage(colleagueProfile, placeholders));
    }

    protected CreateMessageRequestDto getMessage(ColleagueProfile colleagueProfile, Map<String, String> placeholders) {
        var recipient = new RecipientDto();
        recipient.setRecipientUuid(colleagueProfile.getColleague().getColleagueUUID());

        var message = new CreateMessageRequestDto();
        message.setId(UUID.randomUUID());
        message.setSentAt(OffsetDateTime.now());
        message.setSenderName(colleagueProfile.getColleague().getContact().getEmail());
        message.setLink("link"); //TODO what is link?
        message.setTitle(placeholders.get("TITLE"));
        message.setContent(""); // TODO content
        message.setCategory(MessageCategory.OWN);
        message.setRecipients(List.of(recipient));
        return message;
    }
}
