package com.tesco.pma.fs.dao;

import com.tesco.pma.file.api.File;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface FileDAO {

    /**
     * Read all information about file by its identifier
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @return file data
     */
    File read(@Param("fileUuid") UUID fileUuid,
              @Param("includeFileContent") boolean includeFileContent,
              @Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Read all information about files with the latest version applying search, filter and sorting
     *
     * @param requestQuery filter, sorting and pagination
     * @param includeFileContent identifies if include file content
     * @param colleagueUuid an identifier of file's owner
     * @param latest identifies if latest version data needed
     *
     * @return filtered files data
     */
    List<File> findByRequestQuery(@Param("requestQuery") RequestQuery requestQuery,
                                  @Param("includeFileContent") boolean includeFileContent,
                                  @Param("colleagueUuid") UUID colleagueUuid,
                                  @Param("latest") boolean latest);

    /**
     * Save file information to database with maximum+1 version
     *
     * @param file - file data
     * @return the number of the inserted rows
     *
     * Throws exception in case of insert is failed (with constraints etc.) to store file information to database:
     * have no matching status in file_status table,
     * have no matching type in file_type table, etc
     */
    int create(File file);

    /**
     * Delete file information by its uuid
     *
     * @param fileUuid file identifier
     * @param colleagueUuid an identifier of file's owner
     * @return number of deleted rows
     */
    int deleteByUuidAndColleague(@Param("fileUuid") UUID fileUuid,
                                 @Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Delete file information by its path, name and versions
     *
     * @param path          file path
     * @param fileName      file name
     * @param version       file version; if null, remove all versions
     * @param colleagueUuid an identifier of file's owner
     * @return number of deleted rows
     */
    int deleteVersions(@Param("path") String path,
                       @Param("fileName") String fileName,
                       @Param("version") Integer version,
                       @Param("colleagueUuid") UUID colleagueUuid);
}