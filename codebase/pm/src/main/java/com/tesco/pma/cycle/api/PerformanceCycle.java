package com.tesco.pma.cycle.api;

import com.tesco.pma.process.api.model.PMCycleType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PerformanceCycle {
    UUID uuid;
    String organisationKey;
    UUID templateUUID;

    String name;
    UUID createdBy;
    PMCycleStatus status;
    PMCycleType type;

    Instant startTime;
    Instant endTime;

    String properties;
}
