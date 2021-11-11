package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportRequest;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ImportColleaguesDAO {

    default void registerRequest(ImportRequest importRequest) {
        registerRequest(importRequest, Instant.now());
    }

    void registerRequest(@Param("request") ImportRequest importRequest, @Param("now") Instant now);

    default void updateRequest(ImportRequest importRequest) {
        updateRequest(importRequest, Instant.now());
    }

    void updateRequest(@Param("request") ImportRequest importRequest, @Param("now") Instant now);

    void saveError(@Param("error") ImportError importError);

    ImportRequest getRequest(@Param("uuid") UUID uuid);

    List<ImportError> getRequestErrors(@Param("uuid") UUID uuid);
}
