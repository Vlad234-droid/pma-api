package com.tesco.pma.event.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventException;

public class DummyAction implements Action {

    protected static final Logger logger = LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass()); //NOPMD

    @Override
    public EventResponse perform(Event event) throws EventException {
        logger.info("Skip event processing - {}", event);
        return new EventResponseSupport(event, EventResponse.END);
    }

}