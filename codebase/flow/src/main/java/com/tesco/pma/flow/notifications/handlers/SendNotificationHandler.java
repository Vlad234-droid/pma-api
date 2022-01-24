package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.service.SendNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:10
 */
@Slf4j
@Component
@AllArgsConstructor
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SendNotificationHandler extends CamundaAbstractFlowHandler {

    private final List<SendNotificationService> senders;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueProfile = (ColleagueProfile) context.getVariable(FlowParameters.COLLEAGUE_PROFILE.name());
        var templateId = (String) context.getVariable(FlowParameters.CONTACT_TEMPLATE_ID.name());
        var placeholders = getPlaceholders(context);
        senders.forEach(sender -> sender.send(colleagueProfile, templateId, placeholders));
    }

    protected Map<String, String> getPlaceholders(ExecutionContext context) {
        var result = new HashMap<String, String>();
        var placeholdersMap = (Map<String, String>) context.getVariable(FlowParameters.PLACEHOLDERS);
        result.putAll(placeholdersMap);
        return result;
    }
}
