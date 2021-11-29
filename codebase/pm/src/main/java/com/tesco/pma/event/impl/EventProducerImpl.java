package com.tesco.pma.event.impl;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventProducer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventProducerImpl implements EventProducer {

    @Override
    public Event createEvent(ExecutionContext executionContext, String eventType, Map<String, String> props) {
        //TODO
        return null;
    }

}
