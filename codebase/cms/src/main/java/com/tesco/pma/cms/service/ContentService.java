package com.tesco.pma.cms.service;

import com.tesco.pma.cms.model.Content;
import com.tesco.pma.pagination.RequestQuery;

import java.util.List;
import java.util.UUID;

public interface ContentService {

    Content create(Content content);

    List<Content> findByKey(String key);

    List<Content> findByRequestQuery(RequestQuery requestQuery);

    Content findById(UUID uuid);

    void delete(UUID deleteUuid);

    Content update(Content content);
}
