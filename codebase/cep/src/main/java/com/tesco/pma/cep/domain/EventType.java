package com.tesco.pma.cep.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum EventType {

    /**
     * Joiner (mostly for newhires) - established for HCM flow
     */
    @JsonProperty("Joiner")
    JOINER("Joiner"),

    /**
     * Leaver (mostly for leaving Tesco) - established for HCM flow
     */
    @JsonProperty("Leaver")
    LEAVER("Leaver"),

    /**
     * Mover (all other changes) - established for HCM flow
     */
    @JsonProperty("Mover")
    MOVER("Mover"),

    /**
     * But for IAM we also recognize one more:
     *   Reinstatement (when resigning from leaving Tesco and coming back to work)
     */
    @JsonProperty("Reinstatement")
    REINSTATEMENT("Reinstatement");

    private final String value;

    private static final Map<String, EventType> LOOKUP = Arrays.stream(EventType.values())
            .collect(Collectors.toMap(EventType::getValue, Function.identity()));

    EventType(String value) {
        this.value = value;
    }

    public static EventType getEventType(String value) {
        return LOOKUP.get(value);
    }

}