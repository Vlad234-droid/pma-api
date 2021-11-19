package com.tesco.pma.fs.service;

import com.tesco.pma.api.RequestQueryToDictionaryFilterConverter;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.api.FileStatus;
import com.tesco.pma.fs.api.FileType;
import com.tesco.pma.fs.dao.FileDAO;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.fs.api.FileStatus.DRAFT;
import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_REGISTRATION_FAILED;

/**
 * File service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileDAO fileDao;
    private final RequestQueryToDictionaryFilterConverter toDictionaryFilterConverter;

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

    @Override
    public List<File> get(RequestQuery requestQuery, boolean includeFileContent) {
        var statusFilters = Arrays.asList(
                toDictionaryFilterConverter.convert(requestQuery, true, "status", FileStatus.class),
                toDictionaryFilterConverter.convert(requestQuery, false, "status", FileStatus.class)
        );

        var typeFilters = Arrays.asList(
                toDictionaryFilterConverter.convert(requestQuery, true, "type", FileType.class),
                toDictionaryFilterConverter.convert(requestQuery, false, "type", FileType.class)
        );

        return fileDao.findByRequestQuery(requestQuery, statusFilters, typeFilters, includeFileContent);
    }
}
