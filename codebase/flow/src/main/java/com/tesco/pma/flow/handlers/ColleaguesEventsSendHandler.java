package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColleaguesEventsSendHandler extends AbstractEventSendHandler {

    private final ConfigEntryService configEntryService;
    private final EventSender eventSender;

    @Override
    protected void execute(ExecutionContext context) throws Exception {

        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());

        colleagues.stream()
                .map(ColleagueEntity::getUuid)
                .map(uuid -> createEvent(uuid, context))
                .forEach(e -> eventSender.sendEvent(e, null, isErrorSensitiveExpression()));

    }

    protected Event createEvent(UUID colleagueId, ExecutionContext context) {
        var event = new EventSupport(getEventNameExpression());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueId);
        propagateEventParams(event, context);
        return event;
    }

}
