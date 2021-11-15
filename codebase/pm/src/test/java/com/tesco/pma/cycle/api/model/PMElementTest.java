package com.tesco.pma.cycle.api.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class PMElementTest {
    @Test
    void pmElementConstants() {
        check(PMElement.getPropertyNames(), 1);
    }

    @Test
    void pmCycleElementConstants() {
        check(PMCycleElement.getPropertyNames(), 4);
    }

    @Test
    void pmReviewElementConstants() {
        check(PMReviewElement.getPropertyNames(), 9);
    }

    @Test
    void pmFormElementConstants() {
        check(PMFormElement.getPropertyNames(), 3);
    }

    @Test
    void pmTimelinePointElementConstants() {
        check(PMTimelinePointElement.getPropertyNames(), 2);
    }

    private void check(List<String> constants, int size) {
        Assertions.assertEquals(size, constants.size());
    }
}
