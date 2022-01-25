package com.tesco.pma.flow.notifications.service;

import com.tesco.pma.colleague.inbox.web.dto.CreateMessageRequestDto;
import com.tesco.pma.colleague.inbox.web.dto.MessageCategory;
import com.tesco.pma.colleague.inbox.web.dto.RecipientDto;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ColleagueInboxNotificationSender implements SendNotificationService {

    private static final String TITLE_PLACEHOLDER = "TITLE";
    private static final String CONTENT_PLACEHOLDER = "CONTENT";

    private final ColleagueInboxApiClient colleagueInboxApiClient;
    private final FileService fileService;

    @Override
    public void send(ColleagueProfile colleagueProfile, String templateId, Map<String, String> placeholders) {
        var message = getMessage(colleagueProfile, templateId, placeholders);

        log.info("Sending message to Colleague Inbox for {}, Title: {}",
                colleagueProfile.getColleague().getColleagueUUID(), message.getTitle());

        colleagueInboxApiClient.sendNotification(message);
    }

    private CreateMessageRequestDto getMessage(ColleagueProfile colleagueProfile, String templateId,
                                               Map<String, String> placeholders) {
        var recipient = new RecipientDto();
        recipient.setRecipientUuid(colleagueProfile.getColleague().getColleagueUUID());

        var message = new CreateMessageRequestDto();
        message.setId(UUID.randomUUID());
        message.setSentAt(OffsetDateTime.now());
        message.setSenderName(getSenderName(colleagueProfile));
        message.setLink("https://fake/link"); //TODO remove when not required
        message.setTitle(placeholders.get(TITLE_PLACEHOLDER));
        message.setContent(getContent(templateId, placeholders));
        message.setCategory(MessageCategory.OWN);
        message.setRecipients(List.of(recipient));
        return message;
    }

    private String getSenderName(ColleagueProfile colleagueProfile) {
        var profile = colleagueProfile.getColleague().getProfile();
        return profile.getFirstName().charAt(0) + profile.getLastName();
    }

    private String getContent(String templateId, Map<String, String> placeholders) {
        var contentFile = fileService.get(UUID.fromString(templateId), true, null);
        var content = new String(contentFile.getFileContent());
        String content;

        try {
            var contentFile = fileService.get(UUID.fromString(templateId), true);
            content = new String(contentFile.getFileContent());
        } catch (Exception ex) {
            log.info("Couldn't get template {} from FileService", templateId);
            content = placeholders.get(CONTENT_PLACEHOLDER);
        }

        return StringSubstitutor.replace(content, placeholders, "{", "}");

    }
}
