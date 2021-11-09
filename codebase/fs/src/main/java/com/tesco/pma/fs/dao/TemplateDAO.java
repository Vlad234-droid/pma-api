package com.tesco.pma.fs.dao;

import com.tesco.pma.fs.domain.ProcessTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;
import java.util.List;

public interface TemplateDAO {

    /**
     * Read all information about template by its identifier
     *
     * @param templateUuid template identifier
     * @param includeFileContent identifies if include file content
     * @return Process Template data
     */
    ProcessTemplate findByUuid(@Param("templateUuid") UUID templateUuid,
                               @Param("includeFileContent") boolean includeFileContent);

    /**
     * Read all information about all templates
     *
     * @param includeFileContent identifies if include contents of each of the files
     * @return Process Templates data
     */
    List<ProcessTemplate> findAll(@Param("includeFileContent") boolean includeFileContent);

    /**
     * Save template information to database
     *
     * @param processTemplate template data
     * @return the number of the inserted rows
     *
     * Throws exception in case of insert is failed (with constraints etc.) to store template information to database:
     * have no matching status in process_template_status table,
     * have no matching type in process_template_type table, etc
     */
    int save(ProcessTemplate processTemplate);

    /**
     * Gets max version by template path and template name. If nothing found returns 0
     *
     * @param path - template path
     * @param fileName - template file name
     * @return max version or 0 if nothing found
     */
    int getMaxVersion(@Param("path") String path, @Param("fileName") String fileName);
}