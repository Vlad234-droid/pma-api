package com.tesco.pma.fs.service;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.api.RequestQueryToDictionaryFilterConverter;
import com.tesco.pma.dao.DictionaryDAO;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.file.api.FileStatus;
import com.tesco.pma.file.api.FileType;
import com.tesco.pma.fs.dao.FileDAO;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.UploadMetadata;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.pagination.Sort;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.file.api.FileStatus.DRAFT;
import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_REGISTRATION_FAILED;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Sort.SortOrder.DESC;

/**
 * File service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileDAO fileDao;
    private final DictionaryDAO dictionaryDAO;
    private final RequestQueryToDictionaryFilterConverter toDictionaryFilterConverter;

    @Override
    @Transactional
    public File upload(File fileData, UploadMetadata uploadMetadata, UUID creatorId) {
        fileData.setUuid(UUID.randomUUID());

        var currMomentInUTC = Instant.now();
        fileData.setCreatedTime(currMomentInUTC);
        fileData.setCreatedBy(creatorId);

        fileData.setPath(uploadMetadata.getPath());
        if (StringUtils.isNotBlank(uploadMetadata.getFileName())) {
            fileData.setFileName(uploadMetadata.getFileName());
        }
        fileData.setStatus(uploadMetadata.getStatus() == null ? DRAFT : uploadMetadata.getStatus());
        if (uploadMetadata.getType() != null && uploadMetadata.getType().getId() == null && uploadMetadata.getType().getCode() != null) {
            GeneralDictionaryItem dictionaryItem = dictionaryDAO.findByCode("file_type", uploadMetadata.getType().getCode());
            uploadMetadata.getType().setId(dictionaryItem.getId());
        }
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
    public List<File> get(RequestQuery requestQuery, boolean includeFileContent, boolean latest) {
        var statusFilters = Arrays.asList(
                toDictionaryFilterConverter.convert(requestQuery, true, "status", FileStatus.class),
                toDictionaryFilterConverter.convert(requestQuery, false, "status", FileStatus.class)
        );
        return fileDao.findByRequestQuery(requestQuery, statusFilters, includeFileContent, latest);
    }

    @Override
    public File get(String path, String fileName, boolean includeFileContent) {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(new Condition("path", EQUALS, path),
                                              new Condition("file-name", EQUALS, fileName)));

        return get(requestQuery, includeFileContent, true).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "File was not found", fileName));
    }

    @Override
    public List<File> getAllVersions(@NotNull String path, @NotNull String fileName, boolean includeFileContent) {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(new Condition("path", EQUALS, path),
                new Condition("file-name", EQUALS, fileName)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort("version", DESC)));

        return get(requestQuery, includeFileContent, false);
    }
}
