package com.tesco.pma.fs.domain;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.fs.api.FileStatus;
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

    GeneralDictionaryItem type;

    FileStatus status;

    String description;

    Instant fileDate;
}