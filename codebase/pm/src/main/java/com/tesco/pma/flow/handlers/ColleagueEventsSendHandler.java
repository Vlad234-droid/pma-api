package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColleagueEventsSendHandler extends CamundaAbstractFlowHandler {

    private Expression eventNameExpression;
    private Expression isErrorSensitiveExpression;

    private final ConfigEntryService configEntryService;
    private final EventSender eventSender;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) throws Exception {

        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());
        var isThrow = isErrorSensitiveExpression();

        colleagues.stream()
                .map(ColleagueEntity::getUuid)
                .map(this::createEvent)
                .forEach(e -> eventSender.sendEvent(e, null, isThrow));

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
        Objects.requireNonNull(eventNameExpression, "eventNameExpression must be specified");

        return Optional.ofNullable(eventNameExpression.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.VALUE_MUST_BE_SPECIFIED.getCode(),
                                Map.of("value", "eventNameExpression"))));
    }

    public boolean isErrorSensitiveExpression() {
        Objects.requireNonNull(isErrorSensitiveExpression, "isErrorSensitiveExpression must be specified");

        return Boolean.parseBoolean(Optional.ofNullable(isErrorSensitiveExpression.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.VALUE_MUST_BE_SPECIFIED.getCode(),
                                Map.of("value", "isErrorSensitiveExpression")))));
    }

    public void setIsErrorSensitiveExpression(Expression isErrorSensitiveExpression) {
        this.isErrorSensitiveExpression = isErrorSensitiveExpression;
    }


}
