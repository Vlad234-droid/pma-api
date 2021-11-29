package com.tesco.pma.cycle.api;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
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
public class PMColleagueCycle implements Serializable {
    private static final long serialVersionUID = -9187861593097610915L;

    UUID uuid;

    UUID colleagueUuid;
    UUID cycleUuid;

    PMCycleStatus status;

    Instant startTime;
    Instant endTime;

    MapJson properties;

}
