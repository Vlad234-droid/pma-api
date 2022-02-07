package com.tesco.pma.cep.v2.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Three different types of events have been established for HCM flow.
 * But for IAM we also recognize one more.
 *
 * <p>For more information:
 *  @see <a href="https://github.dev.global.tesco.org/24Colleague/colleague-api/wiki/JML-V2">here</a>
 */
@Getter
public enum EventType {

    /**
     * A colleague is defined as joiner in three different cases:
     * <ul>
     * <li>When CF receives data about a colleague from source system with hireDate as today or in the future
     * it will generate event with newJoiner subcategory.</li>
     * <li>If a colleague started working for Tesco before and is just new for CF such a colleague will also be treated
     * as joiner but can be differentiated using sub-category. For such colleagues sub-Category will be importedInCF.</li>
     * <li>When a colleague was previously a leaver and joins again, he is treated as a joiner and distinguished
     * by the rehire subcategory.</li>
     * </ul>
     */
    @JsonProperty("Joiner")
    JOINER("Joiner"),

    /**
     *
     */
    FUTURE_JOINER("FutureJoiner"),

    /**
     * Leaver event will be generated when anyone leaves.
     * There can be three Leaver events - Leaver or FutureLeaver and FutureLeaverCancelled.
     */
    @JsonProperty("Leaver")
    LEAVER("Leaver"),

    /**
     * FutureLeaver is always generated on change in CF
     * and Leaver event if the change has taken place or become effective.
     */
    FUTURE_LEAVER("FutureLeaver"),

    /**
     * FutureLeaverCancelled is always generated on change in CF
     * and Leaver event if the change has taken place or become effective.
     */
    FUTURE_LEAVER_CANCELLED("FutureLeaverCancelled"),

    /**
     * Modification event will be generated when CF receives any changes for a colleague from the source system.
     * There can be two Modification events - Modification or FutureModification.
     */
    MODIFICATION("Modification"),

    /**
     * FutureModification is always generated for future revisions on change in CF and Modification event is generated
     * if the change has taken place or became effective.
     */
    FUTURE_MODIFICATION("FutureModification"),

    /**
     *
     */
    SOURCE_SYSTEM_MODIFICATION("SourceSystemModification"),

    /**
     *
     */
    DELETION("Deletion"),

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