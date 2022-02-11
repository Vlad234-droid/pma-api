package com.tesco.pma.cms.service;


import com.tesco.pma.cms.dao.ContentDAO;
import com.tesco.pma.cms.exception.ContentException;
import com.tesco.pma.cms.exception.ErrorCodes;
import com.tesco.pma.cms.model.Content;
import com.tesco.pma.cms.model.ContentStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final AuditorAware<UUID> auditorAware;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public Content create(Content content) {
        content.setId(UUID.randomUUID());
        content.setCreatedBy(auditorAware.getCurrentAuditor());
        content.setCreatedTime(Instant.now());

        if (content.getVersion() == null) {
            content.setVersion(1);
        }

        if (content.getStatus() == null) {
            content.setStatus(ContentStatus.DRAFT);
        }

        if (1 != contentDAO.create(content)) {

            throw new ContentException(ErrorCodes.CONTENT_CREATE_ERROR.name(),
                    messageSourceAccessor.getMessage(ErrorCodes.CONTENT_CREATE_ERROR));
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

        if (1 != contentDAO.delete(deleteUuid)) {

            throw new ContentException(ErrorCodes.CONTENT_DELETE_ERROR.name(),
                    messageSourceAccessor.getMessage(ErrorCodes.CONTENT_DELETE_ERROR));
        }
    }
}
