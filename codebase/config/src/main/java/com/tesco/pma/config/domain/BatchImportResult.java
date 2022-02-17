package com.tesco.pma.config.domain;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class BatchImportResult {

    @Singular("saved")
    Set<UUID> saved;

    @Singular
    Map<UUID, UUID> withoutManagers;
}
