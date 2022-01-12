package com.tesco.pma.file.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Represents the specified metadata parts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadMetadata {

    String path;

    String fileName;

    FileType type;

    FileStatus status;

    String description;

    Instant fileDate;
}