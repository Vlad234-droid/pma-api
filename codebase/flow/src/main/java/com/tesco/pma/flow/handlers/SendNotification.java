package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.contact.api.DestinationType;
import com.tesco.pma.contact.api.Message;
import com.tesco.pma.contact.api.Recipient;
import com.tesco.pma.service.contact.client.ContactApiClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
        var colleagueUUID = (UUID) context.getEvent().getEventProperty(FlowParameters.COLLEAGUE_UUID.name());
        var templateId = (String) context.getEvent().getEventProperty(FlowParameters.NOTIFICATION_TEMPLATE_ID.name());
        contactApiClient.sendNotification(getMessage(colleagueUUID), templateId);
    }

    private Message getMessage(UUID colleagueId) {
        var message = new Message();
        var recipient = new Recipient();
        recipient.setUuid(colleagueId);
        recipient.setDestination(DestinationType.EMAIL_CC);
        message.setRecipients(List.of(recipient));
        return message;
    }
}
