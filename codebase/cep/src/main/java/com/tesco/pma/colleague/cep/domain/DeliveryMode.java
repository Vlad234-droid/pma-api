package com.tesco.pma.colleague.cep.domain;

import java.util.Arrays;

/*
  @see <a href="https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Event-Producer-Colleague-Changes">here</a>
 */
public enum DeliveryMode {
    JIT,
    IMMEDIATE;

    public static DeliveryMode getDeliveryMode(String feedId) {
        return Arrays.stream(DeliveryMode.values())
                .filter(deliveryMode -> feedId.toLowerCase().contains(deliveryMode.name().toLowerCase()))
                .findFirst()
                .orElse(JIT);
    }

}
