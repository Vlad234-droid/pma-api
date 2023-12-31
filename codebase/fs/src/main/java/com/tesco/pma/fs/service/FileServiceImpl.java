package com.tesco.pma.fs.service;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.DictionaryDAO;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
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
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String FILE_NAME = "fileName";
    private static final String FILE_UUID = "fileUuid";
    private static final String FILE_OWNER = "fileOwner";
    private static final String FILE_PATH = "path";
    private static final String FILE_VERSION = "version";
    private final FileDAO fileDao;
    private final DictionaryDAO dictionaryDAO;

    private final NamedMessageSourceAccessor messageSourceAccessor;

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

        return get(fileData.getUuid(), false, creatorId);
    }

    @Override
    public File get(UUID fileUuid, boolean includeFileContent, UUID colleagueUuid) {
        var params = new HashMap<String, Object>(); // used HashMap because colleagueUuid can be null
        params.put(FILE_UUID, fileUuid);
        params.put(FILE_OWNER, colleagueUuid);

        return Optional.ofNullable(fileDao.read(fileUuid, includeFileContent, colleagueUuid))
                .orElseThrow(() -> notFound(ERROR_FILE_NOT_FOUND, params));
    }

    @Override
    public List<File> get(RequestQuery requestQuery, boolean includeFileContent, UUID colleagueUuid, boolean latest) {
        return fileDao.findByRequestQuery(requestQuery.toDAO(), includeFileContent, colleagueUuid, latest);
    }

    @Override
    public File get(String path, String fileName, boolean includeFileContent, UUID colleagueUuid) {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(new Condition("path", EQUALS, path),
                                              new Condition("file-name", EQUALS, fileName)));

        var params = new HashMap<String, Object>(); // used HashMap because colleagueUuid can be null
        params.put(FILE_NAME, fileName);
        params.put(FILE_OWNER, colleagueUuid);

        return get(requestQuery, includeFileContent, colleagueUuid, true).stream()
                .findFirst()
                .orElseThrow(() -> notFound(ERROR_FILE_NOT_FOUND, params));
    }

    @Override
    public List<File> getAllVersions(String path, String fileName, boolean includeFileContent, UUID colleagueUuid) {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(new Condition("path", EQUALS, path),
                new Condition("file-name", EQUALS, fileName)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort(FILE_VERSION, DESC)));

        return get(requestQuery, includeFileContent, colleagueUuid, false);
    }

    @Override
    @Transactional
    public void delete(UUID fileUuid, UUID colleagueUuid) {
        var deleted = fileDao.deleteByUuidAndColleague(fileUuid, colleagueUuid);
        if (1 != deleted) {
            var params = new HashMap<String, Object>(); // used HashMap because colleagueUuid can be null
            params.put(FILE_UUID, fileUuid);
            params.put(FILE_OWNER, colleagueUuid);

            throw notFound(ERROR_FILE_NOT_FOUND, params);
        }
    }

    @Override
    @Transactional
    public void deleteVersions(String path, String fileName, List<Integer> versions, UUID colleagueUuid) {
        if (CollectionUtils.isEmpty(versions)) {
            fileDao.deleteVersions(path, fileName, null, colleagueUuid);
            return;
        }
        versions.forEach(version -> {
            var deleted = fileDao.deleteVersions(path, fileName, version, colleagueUuid);
            if (1 != deleted) {
                var params = new HashMap<String, Object>(); // used HashMap because colleagueUuid can be null
                params.put(FILE_PATH, path);
                params.put(FILE_NAME, fileName);
                params.put(FILE_VERSION, version);
                params.put(FILE_OWNER, colleagueUuid);

                throw notFound(ERROR_FILE_NOT_FOUND, params);
            }
        });

    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}
