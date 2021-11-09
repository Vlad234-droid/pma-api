package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.List;

/**
 * File service.
 * Implementation classes must be annotated with @org.springframework.validation.annotation.Validated.
 */
public interface FileService {

    /**
     * Upload file and save it to database
     *
     * @param inputStream data of file
     * @param uploadMetadata represents the parts of file metadata
     * @param file is file data
     * @param creatorId represents the creator identifier
     * @return uploaded file
     * @throws IOException in case of file access errors
     * @throws RegistrationException if failed to save file data to database
     */
    File upload(@NotNull InputStream inputStream, @NotNull UploadMetadata uploadMetadata,
                @NotNull MultipartFile file, @NotNull String creatorId) throws IOException;

    /**
     * Read all information about file by its identifier
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @return file data
     * @throws NotFoundException if file by uuid is not found
     */
    File findByUuid(@NotNull UUID fileUuid, boolean includeFileContent);

    /**
     * Read all information about all files
     *
     * @param includeFileContent identifies if include contents of each of the files
     * @return files data
     */
    List<File> findAll(boolean includeFileContent);
}
