package com.tesco.pma.cycle.api;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.file.api.File;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PMCycle implements Serializable {
    private static final long serialVersionUID = -6820865590572330952L;

    UUID uuid;
    String entryConfigKey;
    File template;

    String name;
    ColleagueSimple createdBy;
    PMCycleStatus status;
    PMCycleType type;

    Instant startTime;
    Instant endTime;

    MapJson properties;

    PMCycleMetadata metadata;
}
