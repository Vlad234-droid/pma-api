package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cep.cfapi.v2.configuration.ColleagueChangesProperties;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.service.colleague.ColleagueApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolveColleagueHandler extends CamundaAbstractFlowHandler {

    private final ColleagueChangesProperties colleagueChangesProperties;
    private final ColleagueApiService colleagueApiService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        if (colleagueChangesProperties.isForce()) {
            var colleagueUuid = HandlerUtils.getEventColleagueUuid(context);
            var colleague = colleagueApiService.findColleagueByUuid(colleagueUuid);
            if (colleague != null) {
                context.setVariable(FlowParameters.COLLEAGUE, colleague);
            }
        }
    }
}
