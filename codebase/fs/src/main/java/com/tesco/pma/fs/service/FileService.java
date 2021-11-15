package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * File service.
 * Implementation classes must be annotated with @org.springframework.validation.annotation.Validated.
 */
public interface FileService {

    /**
     * Upload file and save it to database
     *
     * @param fileData is file data
     * @param uploadMetadata represents the parts of file metadata
     * @param creatorId represents the creator identifier
     * @return uploaded file
     * @throws RegistrationException if failed to save file data to database
     */
    File upload(@NotNull File fileData, @NotNull UploadMetadata uploadMetadata, @NotNull String creatorId);

    /**
     * Read all information about file by its identifier
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @return file data
     * @throws NotFoundException if file by uuid is not found
     */
    File get(@NotNull UUID fileUuid, boolean includeFileContent);
}
