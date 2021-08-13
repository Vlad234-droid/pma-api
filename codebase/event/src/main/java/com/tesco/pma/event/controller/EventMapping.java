package com.tesco.pma.event.controller;

import com.tesco.pma.event.Event;

public interface EventMapping {

    Action getAction(Event event);
}
