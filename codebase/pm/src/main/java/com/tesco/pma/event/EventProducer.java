package com.tesco.pma.event;

import com.tesco.pma.bpm.api.flow.ExecutionContext;

import java.util.Map;

public interface EventProducer {

    Event createEvent(ExecutionContext executionContext, String eventType, Map<String, String> props);

}
