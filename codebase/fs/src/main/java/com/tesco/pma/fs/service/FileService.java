package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.UploadMetadata;
import com.tesco.pma.pagination.RequestQuery;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
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
     * @param creatorId represents the creator identifier; it is identifier of colleague
     * @return uploaded file
     * @throws RegistrationException if failed to save file data to database
     */
    File upload(@NotNull File fileData, @NotNull UploadMetadata uploadMetadata, @NotNull UUID creatorId);

    /**
     * Read all information about file by its identifier
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @return file data
     * @throws NotFoundException if file by uuid is not found
     */
    File get(@NotNull UUID fileUuid, boolean includeFileContent, UUID colleagueUuid);

    /**
     * Read all information about file by its identifier without check of it's owner
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @return file data
     * @throws NotFoundException if file by uuid is not found
     */
    default File get(@NotNull UUID fileUuid, boolean includeFileContent) {
        return get(fileUuid, includeFileContent, null);
    }

    /**
     * Read all information about files applying search, filter and sorting
     *
     * @param requestQuery filter, sorting, offset
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @param latest identifies if latest version data needed
     * @return filtered files data
     */
    List<File> get(@NotNull RequestQuery requestQuery, boolean includeFileContent, UUID colleagueUuid, boolean latest);


    /**
     * Read all information about file by its name and path with the latest version
     *
     * @param path file path
     * @param fileName file name
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @return file data
     * @throws NotFoundException if file by name and path is not found
     */
    File get(@NotEmpty String path, @NotEmpty String fileName, boolean includeFileContent, UUID colleagueUuid);

    /**
     * Read all information about file with all versions by its name and path
     *
     * @param path file path
     * @param fileName file name
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @return file data with all versions
     */
    List<File> getAllVersions(@NotEmpty String path, @NotEmpty String fileName, boolean includeFileContent, UUID colleagueUuid);

    /**
     * Delete files by its uuid
     *
     * @param fileUuid      file identifier
     * @param colleagueUuid an identifier of file's owner
     * @throws NotFoundException if file by uuid is not found
     */
    void delete(@NotNull UUID fileUuid, UUID colleagueUuid);

    /**
     * Delete files by its path, name and versions
     *
     * @param path          file path
     * @param fileName      file name
     * @param versions      file versions; if null, remove all versions
     * @param colleagueUuid an identifier of file's owner
     * @throws NotFoundException if file by uuid is not found
     */
    void deleteVersions(@NotEmpty String path, @NotEmpty String fileName, List<Integer> versions, UUID colleagueUuid);
}
