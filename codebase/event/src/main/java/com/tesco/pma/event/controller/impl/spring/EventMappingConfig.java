package com.tesco.pma.event.controller.impl.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tesco.pma.event.controller.EventController;
import com.tesco.pma.event.controller.EventMapping;
import com.tesco.pma.event.controller.EventMonitor;
import com.tesco.pma.event.controller.impl.EventControllerImpl;
import com.tesco.pma.event.controller.impl.SimpleEventMonitor;

@Configuration
@EnableConfigurationProperties
public class EventMappingConfig {

    @Value("${tesco.application.event-monitor-class:HasNotBeenSet}")
    private String eventMonitorClassName;

    @Bean(value = "eventMappings")
    @ConfigurationProperties(prefix = "tesco.application.event-mapping")
    public Map<String, String> eventMappings() {
        return new HashMap<>();
    }

    @Bean
    public EventMapping eventMapping(@Qualifier("eventMappings") Map<String, String> mappings) {
        return new SpringEventMapping(mappings);
    }

    @Bean
    public EventController eventController(EventMapping eventMapping, Optional<List<EventMonitor>> eventMonitors) {
        var eventMonitor = eventMonitors.orElse(Collections.emptyList()).stream()
                                                     .filter(m -> m.getClass().getName().equals(eventMonitorClassName))
                                                     .findAny().orElse(new SimpleEventMonitor());
        return new EventControllerImpl(eventMapping, eventMonitor);
    }
}
