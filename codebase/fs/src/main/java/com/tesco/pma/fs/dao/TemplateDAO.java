package com.tesco.pma.fs.dao;

import org.apache.ibatis.annotations.Param;

public interface TemplateDAO {

    /**
     * Gets max version by template path and template name. If nothing found returns 0
     *
     * @param path - template path
     * @param name - template name
     * @return max version or 0 if nothing found
     */
    int getMaxVersionForTemplate(@Param("path") String path, @Param("name") String name);
}