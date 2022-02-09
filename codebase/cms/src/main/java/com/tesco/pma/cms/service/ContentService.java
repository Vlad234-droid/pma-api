package com.tesco.pma.cms.service;

import com.tesco.pma.cms.model.Content;

import java.util.List;
import java.util.UUID;

public interface ContentService {

    Content create(Content content);
    List<Content> findByKey(String key);
    void delete(UUID deleteUuid);

}
