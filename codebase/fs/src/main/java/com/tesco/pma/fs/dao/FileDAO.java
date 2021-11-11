package com.tesco.pma.fs.dao;

import com.tesco.pma.fs.domain.File;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

public interface FileDAO {

    /**
     * Read all information about file by its identifier
     *
     * @param fileUuid file identifier
     * @param includeFileContent identifies if include file content
     * @return file data
     */
    File findByUuid(@Param("fileUuid") UUID fileUuid,
                    @Param("includeFileContent") boolean includeFileContent);

    /**
     * Save file information to database
     *
     * @param file - file data
     * @return the number of the inserted rows
     *
     * Throws exception in case of insert is failed (with constraints etc.) to store file information to database:
     * have no matching status in file_status table,
     * have no matching type in file_type table, etc
     */
    int save(File file);

    /**
     * Gets max version by file path and file name. If nothing found returns 0
     *
     * @param path - file path
     * @param fileName - file name
     * @return max version or 0 if nothing found
     */
    int getMaxVersion(@Param("path") String path, @Param("fileName") String fileName);
}