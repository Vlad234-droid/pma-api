package com.tesco.pma.colleague.objective.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonalObjective {
    UUID uuid;
    UUID colleagueUuid;
    UUID performanceCycleUuid;
    Integer sequenceNumber;
    String title;
    String description;
    String meets;
    String exceeds;
    GroupObjective groupObjective;
    ObjectiveStatus status;
}
