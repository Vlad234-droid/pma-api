package com.tesco.pma.event.controller.impl.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringEventMapping implements EventMapping, ApplicationContextAware {

    private ApplicationContext context;

    private final Map<String, Class<Action>> eventsToActionsMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Properties must contain event code as a key and classname as a value. Properties object could be included using spring means.
     * Classname should correspond to class implementing {@link Action} interface.
     * Non existed and non Action classes will be skipped
     * @param mappings
     */
    public SpringEventMapping(Map<String, String> mappings) {
        mappings.forEach(this::setMapping);
    }

    @Override
    public Action getAction(Event event) {
        if (event == null) {
            return null;
        }
        Class<Action> actionClass = eventsToActionsMap.get(event.getEventName());
        if (actionClass == null) {
            return null;
        }
        return context.getBean(actionClass);
    }

    /**
     * Sets event name to action mapping class.
     * 
     * @param actionKey
     * @param actionClassName
     */
    private void setMapping(String actionKey, String actionClassName) {
        try {
            Class<?> actionClassCandidate = Class.forName(actionClassName);
            if (!Action.class.isAssignableFrom(actionClassCandidate)) {
                throw new IllegalArgumentException(String.format("%s doesn't implement %s interface", actionClassCandidate, Action.class));
            }
            @SuppressWarnings("unchecked")
            Class<Action> actionClass = (Class<Action>) actionClassCandidate;

            eventsToActionsMap.put(actionKey, actionClass);
            log.info("Event -> Action mapping: {} -> {}", actionKey, actionClass);
        } catch (Exception e) {
            log.error("Action {} cannot be loaded. Exception was occured:{}", actionClassName, e.getMessage(), e);
        }
    }
}
