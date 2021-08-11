package com.tesco.pma.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.11.2019 Time: 16:54
 */
class EventUtilsTest {

    @Test
    void retrieveLongValues() {
        final String propertyName = "IDS";
        final int amount = 2;

        EventSupport event = new EventSupport("test");
        Collection<Long> ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());

        event.putProperty(propertyName, propertyName);
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());

        event.putProperty(propertyName, new ArrayList<>(Arrays.asList("a", "b")));
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());

        event.putProperty(propertyName, new ArrayList<>(Arrays.asList(Boolean.FALSE, Boolean.TRUE)));
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());

        event.putProperty(propertyName, new ArrayList<>(Arrays.asList(1L, 2L)));
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertEquals(amount, ids.size());

        event.putProperty(propertyName, new ArrayList<>(Arrays.asList(1, 2)));
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertEquals(amount, ids.size());

        event.putProperty(propertyName, new ArrayList<>(Arrays.asList("1", "2")));
        ids = EventUtils.retrieveLongValues(event, propertyName);
        assertNotNull(ids);
        assertEquals(amount, ids.size());
    }
}