package com.tesco.pma.configuration.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public interface UuidReadOnlyMixIn {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID getUuid();
}