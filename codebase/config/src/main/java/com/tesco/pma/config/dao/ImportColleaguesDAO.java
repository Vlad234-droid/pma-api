package com.tesco.pma.config.dao;


import com.tesco.pma.config.domain.ImportError;
import com.tesco.pma.config.domain.ImportRequest;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ImportColleaguesDAO {

    /**
     * Register file request
     *
     * @param importRequest - payload
     */
    default void registerRequest(ImportRequest importRequest) {
        registerRequest(importRequest, Instant.now());
    }

    void registerRequest(@Param("request") ImportRequest importRequest, @Param("now") Instant now);

    /**
     * Update request
     *
     * @param importRequest - payload
     */
    default void updateRequest(ImportRequest importRequest) {
        updateRequest(importRequest, Instant.now());
    }

    void updateRequest(@Param("request") ImportRequest importRequest, @Param("now") Instant now);

    /**
     * Save import error into DB
     *
     * @param importError - error object
     */
    void saveError(@Param("error") ImportError importError);

    /**
     * Get request by identifier
     *
     * @param uuid - request identifier
     * @return request
     */
    ImportRequest getRequest(@Param("uuid") UUID uuid);

    /**
     * Get list of import errors
     *
     * @param uuid - request identifier
     * @return list of errors
     */
    List<ImportError> getRequestErrors(@Param("uuid") UUID uuid);
}
