package com.tesco.pma.colleague.cep.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
  @see <a href="https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Event-Producer-Colleague-Changes">here</a>
 */
@Getter
public enum DeliveryMode {

    JIT("colleagues-jit-v1"),
    IMMEDIATE("colleagues-immediate-v1");

    private final String value;

    private static final Map<String, DeliveryMode> LOOKUP = Arrays.stream(DeliveryMode.values())
            .collect(Collectors.toMap(DeliveryMode::getValue, Function.identity()));

    DeliveryMode(String value) {
        this.value = value;
    }

    public static DeliveryMode getDeliveryMode(String value) {
        return LOOKUP.get(value);
    }

}
