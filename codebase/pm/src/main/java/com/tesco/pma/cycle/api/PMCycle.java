package com.tesco.pma.cycle.api;

import com.tesco.pma.cycle.api.model.PMCycleMetadata;
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
public class PMCycle {
    UUID uuid;
    String entryConfigKey;
    UUID templateUUID;

    String name;
    UUID createdBy;
    PMCycleStatus status;
    PMCycleType type;

    Instant startTime;
    Instant endTime;

    String properties;

    String jsonMetadata;
    PMCycleMetadata metadata;
}
