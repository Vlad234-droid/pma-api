package com.tesco.pma.event.controller.impl.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.controller.Action;

class SpringEventMappingTest {

    private static final String SUCCESS_CODE = "success"; 

    private final Map<String, String> properties = new HashMap<>();

    private ApplicationContext context;

    @BeforeEach
    void init() {
        properties.clear();
        context = mock(ApplicationContext.class);
        Action mock = mock(Action.class);
        when(context.getBean(mock.getClass())).then(invocation -> mock);
        properties.put(SUCCESS_CODE, mock.getClass().getName());
    }

    @Test
    void create() {
        String notActionClass = "notActionClass";
        String notPresentClass = "notPresentClass";

        properties.put(notActionClass, this.getClass().getName());
        properties.put(notPresentClass, "SomeNotPresentClass");

        SpringEventMapping mapping = initMapping();

        Assertions.assertNotNull(mapping.getAction(new EventSupport(SUCCESS_CODE)));
        Assertions.assertNull(mapping.getAction(new EventSupport(notActionClass)));
        Assertions.assertNull(mapping.getAction(new EventSupport(notPresentClass)));
    }

    private SpringEventMapping initMapping() {
        SpringEventMapping springEventMapping = new SpringEventMapping(properties);
        springEventMapping.setApplicationContext(context);
        return springEventMapping;
    }

    @Test
    void search() {
        SpringEventMapping mapping = initMapping();

        Assertions.assertNotNull(mapping.getAction(new EventSupport(SUCCESS_CODE)));
        Assertions.assertNull(mapping.getAction(new EventSupport("notPresent")));
    }
}
