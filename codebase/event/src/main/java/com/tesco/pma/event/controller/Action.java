package com.tesco.pma.event.controller;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;

public interface Action {
    EventResponse perform(Event event) throws EventException;
}
