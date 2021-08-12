package com.tesco.pma.event.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventListener;
import com.tesco.pma.event.controller.EventController;
import com.tesco.pma.event.controller.EventException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/events")
public class SpringRestEventListener implements EventListener {

    @Autowired
    EventController eventController;

    @Qualifier("restEventExecutor")
    @Autowired
    private Executor executor;

    @Override
    @PostMapping
    public void handle(@RequestBody Event event) throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                eventController.processEvent(event);
            } catch (EventException e) {
                log.error("Fail to process " + event, e);
            }
        }, executor);
    }
}

