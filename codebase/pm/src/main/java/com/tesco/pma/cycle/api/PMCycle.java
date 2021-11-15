package com.tesco.pma.cycle.api;

import com.tesco.pma.api.MapJson;
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
    PMCycleUserProfile createdBy;
    PMCycleStatus status;
    PMCycleType type;

    Instant startTime;
    Instant endTime;

    MapJson properties;

    String jsonMetadata;
}
