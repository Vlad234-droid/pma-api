package com.tesco.pma.cep.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tesco.pma.api.Identified;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

/**
 * This class describes a structure of payload section CEP events.
 *
 * <p>For more information:
 * @see <a href="https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Event-Producer-Colleague-Changes">here</a>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties({"id"})
public class ColleagueChangeEventPayload implements Identified<UUID> {

    /**
     * eventType is a field that can have only 4 values: Joiner or Mover or Leaver or Reinstatement depending on the change.
     */
    EventType eventType;

    /**
     * colleagueUUID is a field in UUID format. It holds the information whose data has changes.
     * It is a uuid of a colleague taken from Identity
     */
    @JsonProperty("colleagueUUID")
    UUID colleagueUuid;

    /**
     * effectiveDate is a field in format YYYY-MM-DD. It stores information since when are these changes applicable.
     * Usually the date provided here is the first date of the changes being valid.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate effectiveDate;

    /**
     * changedAttributes is a field in an array of strings format.
     * Values in the array are names of the fields that have changed for the colleague.
     */
    Collection<String> changedAttributes;

    @Override
    public UUID getId() {
        return colleagueUuid;
    }

}
