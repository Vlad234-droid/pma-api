package com.tesco.pma.healthcheck;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/**
 * Status for Health Indicator
 */
@RequiredArgsConstructor
public enum HealthStatus {
    OK("Ok"),
    FAIL("Fail"),
    DEGRADED("Degraded");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}