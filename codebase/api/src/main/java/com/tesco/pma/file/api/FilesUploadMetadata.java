package com.tesco.pma.file.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Represents upload metadata for files
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilesUploadMetadata implements Serializable {
    private static final long serialVersionUID = -6087516482098984582L;

    private List<UploadMetadata> uploadMetadataList;
}