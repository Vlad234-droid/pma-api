package com.tesco.pma.config.domain;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ImportRequest {
    private UUID uuid;
    private String fileName;
    private ImportRequestStatus status;
    private String createdBy;
    private Instant creationTime;
    private Instant lastUpdateTime;
}
