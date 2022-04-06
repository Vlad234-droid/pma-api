package com.tesco.pma.flow.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cep.cfapi.v2.configuration.ColleagueChangesProperties;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.service.colleague.ColleagueApiService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tesco.pma.cep.cfapi.v2.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class ResolveColleagueHandler extends CamundaAbstractFlowHandler {

    private static final String COLLEAGUE_UUID = "colleagueUuid";

    private final ColleagueChangesProperties colleagueChangesProperties;
    private final ColleagueApiService colleagueApiService;
    private final ObjectMapper objectMapper;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueUuid = HandlerUtils.getEventColleagueUuid(context);
        context.setVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid);

        if (colleagueChangesProperties.isForce()) {
            var colleague = colleagueApiService.findColleagueByUuid(colleagueUuid);
            if (colleague != null) {
                context.setVariable(FlowParameters.COLLEAGUE, colleague);
            } else {
                throw new BpmnError(COLLEAGUE_NOT_FOUND.getCode(), messageSourceAccessor.getMessage(
                        COLLEAGUE_NOT_FOUND, Map.of(COLLEAGUE_UUID, colleagueUuid)
                ));
            }
        } else {
            var colleagueJson = HandlerUtils.getEventProperty(context, FlowParameters.COLLEAGUE.name(), String.class);
            var colleague = objectMapper.readValue(colleagueJson, Colleague.class);
            context.setVariable(FlowParameters.COLLEAGUE, colleague);
        }
    }
}
