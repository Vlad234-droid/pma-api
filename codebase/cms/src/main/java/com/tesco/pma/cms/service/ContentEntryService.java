package com.tesco.pma.cms.service;

import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.pagination.RequestQuery;

import java.util.List;
import java.util.UUID;

public interface ContentEntryService {

    ContentEntry create(ContentEntry contentEntry);

    List<ContentEntry> findByKey(String key);

    List<ContentEntry> findByRequestQuery(RequestQuery requestQuery);

    ContentEntry findById(UUID uuid);

    void delete(UUID deleteUuid);

    ContentEntry update(ContentEntry contentEntry);
}
