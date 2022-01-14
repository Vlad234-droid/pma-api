package com.tesco.pma.file.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.api.Identified;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * File model.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class File extends UploadMetadata implements Serializable, Identified<UUID> {

    private static final long serialVersionUID = 7964630775764614493L;

    UUID uuid;

    Integer version;

    String fileName;

    Integer fileLength;

    UUID createdBy;

    Instant createdTime;

    byte[] fileContent;

    @Override
    @JsonIgnore
    public UUID getId() {
        return uuid;
    }
}