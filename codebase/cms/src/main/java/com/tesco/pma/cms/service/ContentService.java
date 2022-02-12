package com.tesco.pma.cms.service;

import com.tesco.pma.cms.model.Content;

import java.util.List;
import java.util.UUID;

public interface ContentService {

    Content create(Content content);

    List<Content> findByKey(String key);

    Content findById(UUID uuid);

    void delete(UUID deleteUuid);

    Content update(Content content);
}
