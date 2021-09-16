package com.tesco.pma.colleague.cep.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum EventType {

    @JsonProperty("Joiner")
    JOINER("Joiner"),

    @JsonProperty("Mover")
    MOVER("Mover"),

    @JsonProperty("Leaver")
    LEAVER("Leaver"),

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