package com.tesco.pma.objective.domain;


import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupObjective {
    UUID uuid;
    UUID businessUnitUuid;
    UUID performanceCycleUuid;
    Integer sequenceNumber;
    String title;
    Integer version;
    ObjectiveStatus status;
}
