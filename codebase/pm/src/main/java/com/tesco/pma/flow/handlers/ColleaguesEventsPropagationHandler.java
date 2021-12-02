package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSender;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColleaguesEventsPropagationHandler extends CamundaAbstractFlowHandler {

    private static final String COLLEAGUE_UUID_PROP = "colleagueUuid";

    private Expression injectedValue;

    private final ConfigEntryService configEntryService;
    private final EventSender eventSender;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());

        colleagues.stream()
                .map(ColleagueEntity::getUuid)
                .map(this::createEvent)
                .forEach(eventSender::send);

    }

    private Event createEvent(UUID colleagueId){
        var event = new EventSupport(injectedValue.getExpressionText());
        event.putProperty(COLLEAGUE_UUID_PROP, colleagueId);
        return event;
    }

    public void setInjectedValue(Expression expression) {
        this.injectedValue = expression;
    }
}
