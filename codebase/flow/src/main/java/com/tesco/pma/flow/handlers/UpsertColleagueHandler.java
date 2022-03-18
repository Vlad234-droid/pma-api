package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpsertColleagueHandler extends CamundaAbstractFlowHandler {

    private final ProfileService profileService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleague = context.getVariable(FlowParameters.COLLEAGUE, Colleague.class);
        profileService.updateColleague(colleague);
    }
}
