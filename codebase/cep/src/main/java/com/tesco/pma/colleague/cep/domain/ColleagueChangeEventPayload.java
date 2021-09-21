package com.tesco.pma.colleague.cep.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tesco.pma.api.Identified;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Value
@Accessors(fluent = true)
/*
  @see <a href="https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Event-Producer-Colleague-Changes">here</a>
 */
public class ColleagueChangeEventPayload implements Identified<UUID> {

    EventType eventType;

    UUID colleagueUUID;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate effectiveDate;

    Collection<String> changedAttributes;

    @Override
    public UUID getId() {
        return colleagueUUID;
    }

}
