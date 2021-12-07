package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColleagueEventsSendHandler extends CamundaAbstractFlowHandler {

    private Expression eventNameExpression;

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

    protected Event createEvent(UUID colleagueId) {
        var event = new EventSupport(getEventNameExpression());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueId);
        return event;
    }

    public void setEventNameExpression(Expression expression) {
        this.eventNameExpression = expression;
    }

    public String getEventNameExpression() {
        Objects.requireNonNull(eventNameExpression, "injectedValue must be specified");
        return Optional.ofNullable(eventNameExpression.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(FlowMessages.FLOW_ERROR_RUNTIME
                        .format("Wrong injectedValue: %s", eventNameExpression.getExpressionText())));
    }
}
