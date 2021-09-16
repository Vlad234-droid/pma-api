package com.tesco.pma.colleague.cep.domain;

import com.tesco.pma.api.Identified;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Value
@Accessors(fluent = true)
public class ColleagueChangeEventPayload implements Identified<UUID> {

    EventType eventType;

    UUID colleagueUUID;

    LocalDate effectiveDate;

    Collection<String> changedAttributes;

    @Override
    public UUID getId() {
        return colleagueUUID;
    }

}
