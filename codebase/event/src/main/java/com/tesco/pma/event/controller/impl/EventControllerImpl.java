package com.tesco.pma.event.controller.impl;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.EventController;
import com.tesco.pma.event.controller.EventException;
import com.tesco.pma.event.controller.EventMapping;
import com.tesco.pma.event.controller.EventMonitor;

public class EventControllerImpl implements EventController {

    protected final EventMapping eventMapping;
    protected final EventMonitor eventMonitor;

    public EventControllerImpl(EventMapping mapping, EventMonitor eventMonitor) {
        this.eventMapping = mapping;
        this.eventMonitor = eventMonitor;
    }

    @Override
    public void processEvent(final Event event) throws EventException {
        processEventReturnResponse(event);
    }

    @Override
    public EventResponse processEventReturnResponse(Event event) throws EventException {
        try {
            if (event == null) {
                throw new IllegalArgumentException("Event is undefined");
            }
            eventMonitor.occurred(event);

            final var action = eventMapping.getAction(event);
            if (action != null) {
                eventMonitor.start(event);

                var response = action.perform(event);
                eventMonitor.end(event, response);
                return response;
            } else {
                eventMonitor.notMapped(event);
                var message = "There is no action for the event: " + event.getEventName();
                return new EventResponseSupport(event.getEventName(), EventResponse.END, message);
            }
        } catch (EventException e) {
            eventMonitor.runtimeError(event, e);
            throw e;
        } catch (Exception e) {
            eventMonitor.unexpectedError(event, e);
            throw new EventException(event, "Unexpected exception caught during processing event", e);
        }
    }
}
