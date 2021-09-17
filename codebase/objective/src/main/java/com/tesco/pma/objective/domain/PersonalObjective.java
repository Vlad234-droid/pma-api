package com.tesco.pma.objective.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
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
