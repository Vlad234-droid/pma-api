package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.service.SendNotificationService;
import com.tesco.pma.tip.service.TipService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
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

    private final List<SendNotificationService> senders;
    private final TipService tipService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueProfile = (ColleagueProfile) context.getEvent().getEventProperty(FlowParameters.COLLEAGUE_PROFILE.name());
        var templateId = (String) context.getEvent().getEventProperty(FlowParameters.CONTACT_TEMPLATE_ID.name());
        var placeholders = getPlaceholders(colleagueProfile, context);
        senders.forEach(sender -> sender.send(colleagueProfile, templateId, placeholders));
    }

    protected Map<String, String> getPlaceholders(ColleagueProfile colleagueProfile, ExecutionContext context) {
        var result = new HashMap<String, String>();
        result.put("colleagueUUID", colleagueProfile.getColleague().getColleagueUUID().toString());

        var tipUUID = (UUID) context.getVariable(FlowParameters.TIP_UUID);

        if (tipUUID != null ) {
            var tip = tipService.findOne(tipUUID);
            result.put("tipContent", tip.getDescription());
        }

        return result;
    }
}
