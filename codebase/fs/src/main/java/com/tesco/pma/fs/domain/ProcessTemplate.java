package com.tesco.pma.fs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tesco.pma.api.Identified;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

/**
 * Process Template model.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProcessTemplate extends UploadMetadata implements Identified<UUID> {

    UUID uuid;

    Integer version;

    String fileName;

    Integer fileLength;

    byte[] fileContent;

    String createdBy;

    Instant createdTime;

    @Override
    @JsonIgnore
    public UUID getId() {
        return uuid;
    }
}