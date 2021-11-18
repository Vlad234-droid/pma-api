package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.dao.FileDAO;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.fs.domain.FileStatus.DRAFT;
import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_REGISTRATION_FAILED;

/**
 * File service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileDAO fileDao;

    @Override
    @Transactional
    public File upload(File fileData, UploadMetadata uploadMetadata, UUID creatorId) {
        fileData.setUuid(UUID.randomUUID());

        var currMomentInUTC = Instant.now();
        fileData.setCreatedTime(currMomentInUTC);
        fileData.setCreatedBy(creatorId);

        fileData.setPath(uploadMetadata.getPath());
        fileData.setStatus(uploadMetadata.getStatus() == null ? DRAFT : uploadMetadata.getStatus());
        fileData.setType(uploadMetadata.getType());
        fileData.setDescription(uploadMetadata.getDescription());
        fileData.setFileDate(uploadMetadata.getFileDate() == null ? currMomentInUTC : uploadMetadata.getFileDate());

        var insertedRows = fileDao.create(fileData);
        if (insertedRows != 1) {
            throw new RegistrationException(ERROR_FILE_REGISTRATION_FAILED.name(),
                    "Failed to save File to database", fileData.getFileName());
        }

        return get(fileData.getUuid(), false);
    }

    @Override
    public File get(UUID fileUuid, boolean includeFileContent) {
        return Optional.ofNullable(fileDao.read(fileUuid, includeFileContent))
                .orElseThrow(() -> new NotFoundException(ERROR_FILE_NOT_FOUND.name(),
                        "File was not found", fileUuid.toString()));

    }
}
