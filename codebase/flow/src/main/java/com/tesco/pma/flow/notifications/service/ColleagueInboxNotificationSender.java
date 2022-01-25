package com.tesco.pma.flow.notifications.service;

import com.tesco.pma.colleague.inbox.web.dto.CreateMessageRequestDto;
import com.tesco.pma.colleague.inbox.web.dto.MessageCategory;
import com.tesco.pma.colleague.inbox.web.dto.RecipientDto;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ColleagueInboxNotificationSender implements SendNotificationService {

    private final ColleagueInboxApiClient colleagueInboxApiClient;
    private final FileService fileService;

    @Override
    public void send(ColleagueProfile colleagueProfile, String templateId, Map<String, String> placeholders) {
        colleagueInboxApiClient.sendNotification(getMessage(colleagueProfile, templateId, placeholders));
    }

    private CreateMessageRequestDto getMessage(ColleagueProfile colleagueProfile, String templateId,
                                               Map<String, String> placeholders) {
        var recipient = new RecipientDto();
        recipient.setRecipientUuid(colleagueProfile.getColleague().getColleagueUUID());

        var message = new CreateMessageRequestDto();
        message.setId(UUID.randomUUID());
        message.setSentAt(OffsetDateTime.now());
        message.setSenderName(colleagueProfile.getColleague().getContact().getEmail());
        message.setLink("link"); //TODO what is link?
        message.setTitle(placeholders.get("TITLE"));
        message.setContent(getContent(templateId, placeholders));
        message.setCategory(MessageCategory.OWN);
        message.setRecipients(List.of(recipient));
        return message;
    }

    private String getContent(String templateId, Map<String, String> placeholders) {
        var contentFile = fileService.get(UUID.fromString(templateId), true);
        var content = new String(contentFile.getFileContent());
        return StringSubstitutor.replace(content, placeholders, "{", "}");

    }
}
