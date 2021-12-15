package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.contact.api.DestinationType;
import com.tesco.pma.contact.api.Message;
import com.tesco.pma.contact.api.Recipient;
import com.tesco.pma.service.contact.client.ContactApiClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:10
 */
@Slf4j
@Component
@AllArgsConstructor
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SendNotification extends CamundaAbstractFlowHandler {

    private final ContactApiClient contactApiClient;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueProfile = (ColleagueProfile) context.getEvent().getEventProperty(FlowParameters.COLLEAGUE_PROFILE.name());
        var templateId = (String) context.getEvent().getEventProperty(FlowParameters.CONTACT_TEMPLATE_ID.name());

        contactApiClient.sendNotification(
                getMessage(colleagueProfile.getColleague().getColleagueUUID(),
                        getPlaceholders(colleagueProfile)), templateId);
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

    protected Map<String, String> getPlaceholders(ColleagueProfile colleagueProfile) {
        return Map.of("colleagueUUID", colleagueProfile.getColleague().getColleagueUUID().toString());
    }
}
