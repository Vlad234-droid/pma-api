package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Component
public class ColleaguesEventsSendHandler extends AbstractEventSendHandler {

    private final ConfigEntryService configEntryService;
    private final EventSender eventSender;

    public ColleaguesEventsSendHandler(NamedMessageSourceAccessor messageSourceAccessor,
                                       ConfigEntryService configEntryService,
                                       EventSender eventSender) {
        super(messageSourceAccessor);
        this.configEntryService = configEntryService;
        this.eventSender = eventSender;
    }


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

    private void propagateEventParams(EventSupport event, ExecutionContext context) {
        if (!(context.getVariable(FlowParameters.EVENT_PARAMS) instanceof Map)) {
            return;
        }

        var paramMap = (Map<String, Object>) context.getVariable(FlowParameters.EVENT_PARAMS);

        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            event.putProperty(entry.getKey(), (Serializable) entry.getValue());
        }
    }


}