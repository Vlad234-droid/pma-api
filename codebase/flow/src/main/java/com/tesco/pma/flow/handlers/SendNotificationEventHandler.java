package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.NotificationEvents;
import com.tesco.pma.flow.notifications.NotificationTypes;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SendNotificationEventHandler extends AbstractNotificationEventHandler {

    private final EventSender eventSender;

    public SendNotificationEventHandler(NamedMessageSourceAccessor messageSourceAccessor,
                                      EventSender eventSender) {
        super(messageSourceAccessor);
        this.eventSender = eventSender;
    }

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        eventSender.sendEvent(createEvent(context), null, isErrorSensitiveExpression());
    }

    protected Event createEvent(ExecutionContext context) {
        var eventName = getEventNameExpression();
        var colleagueUUID = (UUID) context.getVariable(FlowParameters.COLLEAGUE_UUID);
        var event = new EventSupport(eventName);
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueUUID);
        var notificationEvent = NotificationEvents.valueOf(eventName);

        if (NotificationTypes.REVIEW == notificationEvent.getType()) {
            event.putProperty(FlowParameters.REVIEW_TYPE.name(), context.getVariable(FlowParameters.REVIEW_TYPE));
        }

        if (NotificationTypes.CYCLE == notificationEvent.getType()) {
            event.putProperty(FlowParameters.QUARTER.name(), context.getVariable(FlowParameters.QUARTER));
        }


        return event;
    }
}
