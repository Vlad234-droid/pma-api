package com.tesco.pma.cms.service;


import com.tesco.pma.cms.dao.ContentDAO;
import com.tesco.pma.cms.model.Content;
import com.tesco.pma.configuration.audit.AuditorAware;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final AuditorAware<UUID> auditorAware;


    @Override
    @Transactional
    public Content create(Content content) {
        content.setId(UUID.randomUUID());
        content.setCreatedBy(auditorAware.getCurrentAuditor());

        if(1 != contentDAO.create(content)) {
            throw new RuntimeException();
        }

        return content;
    }

    @Override
    public List<Content> findByKey(String key) {
        return contentDAO.findByKey(key);
    }

    @Override
    @Transactional
    public void delete(UUID deleteUuid) {

        if(1 != contentDAO.delete(deleteUuid)) {
            throw new RuntimeException();
        }
    }
}
