package com.tesco.pma.cycle.api;

import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.model.PMCycleType;
import lombok.Data;

import java.util.UUID;

@Data
public class PerformanceCycle {
    UUID uuid;
    String organisationKey;
    UUID templateUUID;

    String name;
    UUID createdBy;
    PMCycleStatus status;

    //cycle details
    PMCycleType type;

    String properties;
}
