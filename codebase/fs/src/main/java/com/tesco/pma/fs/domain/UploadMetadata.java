package com.tesco.pma.fs.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Represents the specified metadata parts in process template with multipart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadMetadata {

    String path;

    ProcessTemplateType type;

    ProcessTemplateStatus status;

    String description;

    Instant fileDate;
}