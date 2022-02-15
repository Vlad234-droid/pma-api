package com.tesco.pma.cep.v2.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tesco.pma.api.Identified;
import com.tesco.pma.colleague.api.Colleague;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This class describes a structure of payload section CEP V2 events.
 *
 * <p>For more information:
 * @see <a href="https://github.dev.global.tesco.org/24Colleague/colleague-api/wiki/JML-V2">here</a>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties({"id"})
public class ColleagueChangeEventPayload implements Identified<UUID> {

    /**
     * e.g Joiner, Leaver, Modification, FutureModification etc.
     */
    EventType eventType;

    /**
     * colleagueUUID is a field in UUID format. It holds the information whose data has changes.
     * It is a uuid of a colleague taken from Identity
     */
    @JsonProperty("colleagueUUID")
    UUID colleagueUuid;

    /**
     * Effectivity of the revision contained in the 'current' field
     * Those 2 fields may be omitted in case of for example
     * FutureLeaverCancelled where they don't make much sense
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate effectiveFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate effectiveTo;

    /**
     * May be omitted if specific even type has no subcategories
     */
    @JsonProperty("subcategories")
    List<Subcategory> subcategories;

    /**
     * Field path enumeration of the fields that are different between the colleague data
     * in the 'previous' and 'current' fields.
     * This field is omitted when there is no 'previous' field in the event.
     */
    Collection<String> changedAttributes;

    /**
     * In case of events which represent the immediate change of the Colleague (e.g. Modification),
     * it refers to the colleague state before the change.
     * In case of some Future* events, see the description of the given event type.
     * see Colleague Revision data example below for the structure
     */
    Colleague previous;

    /**
     * In case of events which represent the immediate change of the Colleague (e.g. Modification),
     * it refers to the current state after the change.
     * In case of Future* events, it refers to the state of the colleague as of some date in future (effectivity.from).
     * see Colleague Revision data example below for the structure.
     */
    Colleague current;

    @Override
    public UUID getId() {
        return colleagueUuid;
    }

}
