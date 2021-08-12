package com.tesco.pma.event.controller.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventMapping;

/**
 * Maps events' names to actions' instances. 
 */
public class BaseEventMapping implements EventMapping {

    private static final Logger logger = LoggerFactory.getLogger(BaseEventMapping.class.getName()); //NOPMD

    /**
     * Map: events' names -> actions' instances.
     * Map is not synchronized now because it is filled only once and
     * there are no editing methods in the current implementation of the class.
     */
    private final Map<String, Action> eventsToActionsMap = new HashMap<>();

    /**
     * Properties must contain event code as a key and classname as a value.
     * Classname should correspond to class implementing {@link Action} interface.
     * Non existed and non Action classes will be skipped
     * @param properties properties
     */

    public BaseEventMapping(Properties properties) {
        properties.forEach(this::setMapping);
    }

    /**
     * Sets event name to action mapping (all actions is pre-instantiated).
     * @param actionKey action key
     * @param actionClassName clas name
     */
    private void setMapping(Object actionKey, Object actionClassName) {
        try {
            @SuppressWarnings("unchecked")
            Class<Action> actionClass = (Class<Action>) Class.forName((String) actionClassName);
            Action actionInstance = actionClass.newInstance();
            eventsToActionsMap.put((String) actionKey, actionInstance);
            if (logger.isInfoEnabled()) {
                logger.info("Event -> Action mapping: {} -> {}", actionKey, actionInstance);
            }
        } catch (Exception e) {
            logger.error("Action {} cannot be loaded. Exception was occured:{}", actionClassName, e.getMessage(), e);
        }
    }

    /**
     * Looks up a action for the given event.
     *
     * @param event current event.
     * @return the looked up action instance, or null.
     */
    @Override
    public Action getAction(Event event) {
        return eventsToActionsMap.get(event.getEventName());
    }
}
