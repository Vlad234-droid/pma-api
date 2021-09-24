package com.tesco.pma.cep.domain;

/**
 * CEP supports two kinds of events flows: Just in time flow and Immediate flow
 *
 * <p>For more information:
 *  @see <a href="https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Event-Producer-Colleague-Changes">here</a>
 */
public enum DeliveryMode {
    JIT,
    IMMEDIATE;
}
