package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.service.SendNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:10
 */
@Slf4j
@Component
@AllArgsConstructor
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SendNotification extends CamundaAbstractFlowHandler {

    private final List<SendNotificationService> senders;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueProfile = (ColleagueProfile) context.getEvent().getEventProperty(FlowParameters.COLLEAGUE_PROFILE.name());
        var templateId = (String) context.getEvent().getEventProperty(FlowParameters.CONTACT_TEMPLATE_ID.name());
        var placeholders = getPlaceholders(colleagueProfile);
        senders.forEach(sender -> sender.send(colleagueProfile, templateId, placeholders));
    }

    protected Map<String, String> getPlaceholders(ColleagueProfile colleagueProfile) {
        return Map.of("colleagueUUID", colleagueProfile.getColleague().getColleagueUUID().toString());
    }
}
