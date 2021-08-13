package com.tesco.pma.event.controller;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;

public interface EventController {
    
    void processEvent(final Event event) throws EventException;

    EventResponse processEventReturnResponse(final Event event) throws EventException;
}