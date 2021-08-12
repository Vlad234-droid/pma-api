package com.tesco.pma.event.controller.impl;

import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.controller.Action;

class BaseEventMappingTest {

    private final Properties properties = new Properties();

    @Test
    void create() {
        String code = "action";
        String notActionClass = "notActionClass";
        String notPresentClass = "notPresentClass";

        properties.put(code, mock(Action.class).getClass().getName());
        properties.put(notActionClass, this.getClass().getName());
        properties.put(notPresentClass, "SomeNotPresentClass");

        BaseEventMapping mapping = new BaseEventMapping(properties);

        Assertions.assertNotNull(mapping.getAction(new EventSupport(code)));
        Assertions.assertNull(mapping.getAction(new EventSupport(notActionClass)));
        Assertions.assertNull(mapping.getAction(new EventSupport(notPresentClass)));
    }

    @Test
    void search() {
        String code = "present";
        
        properties.put(code, mock(Action.class).getClass().getName());
        BaseEventMapping mapping = new BaseEventMapping(properties);

        Assertions.assertNotNull(mapping.getAction(new EventSupport(code)));
        Assertions.assertNull(mapping.getAction(new EventSupport("notPresent")));
    }
}
