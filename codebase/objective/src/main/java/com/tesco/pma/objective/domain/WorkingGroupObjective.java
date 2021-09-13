package com.tesco.pma.objective.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkingGroupObjective {
    UUID businessUnitUuid;
    UUID performanceCycleUuid;
    Integer sequenceNumber;
    Integer version;
    UUID groupObjectiveUuid;
    String updaterId;
    Instant updateTime;
}
