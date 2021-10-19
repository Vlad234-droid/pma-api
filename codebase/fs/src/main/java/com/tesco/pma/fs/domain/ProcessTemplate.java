package com.tesco.pma.fs.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

/**
 * Process Template model.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProcessTemplate {

    UUID uuid;
    Integer version;
    String templatePath;
    ProcessTemplateType type;
    ProcessTemplateStatus status;
    String description;

    String createdBy;
    Instant createdTime;

    String fileName;
    Instant fileDate;
    Integer fileLength;
    byte[] fileContent;
}
