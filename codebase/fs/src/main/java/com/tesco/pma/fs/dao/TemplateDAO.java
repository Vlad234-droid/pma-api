package com.tesco.pma.fs.dao;

import com.tesco.pma.fs.domain.ProcessTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

public interface TemplateDAO {

    /**
     * Read all information about template by its identifier
     *
     * @param templateUuid template identifier
     * @return Process Template data
     */
    ProcessTemplate readTemplateByUuid(@Param("templateUuid") UUID templateUuid);

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
    int saveTemplate(ProcessTemplate processTemplate);

    /**
     * Gets max version by template path and template name. If nothing found returns 0
     *
     * @param path - template path
     * @param fileName - template file name
     * @return max version or 0 if nothing found
     */
    int getMaxVersionForTemplate(@Param("path") String path, @Param("fileName") String fileName);
}