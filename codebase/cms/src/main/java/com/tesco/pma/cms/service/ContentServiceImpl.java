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
            content.setStatus(ContentStatus.PUBLISHED);
        }

        if (1 != contentDAO.create(content)) {
            throw contentException(ErrorCodes.CONTENT_CREATE_ERROR);
        }

        return content;
    }

    @Override
    public List<Content> findByKey(String key) {
        return contentDAO.findByKey(key);
    }

    @Override
    public Content findById(UUID uuid) {
        var content = contentDAO.findById(uuid);

        if (content == null) {
            throw contentException(ErrorCodes.CONTENT_NOT_FOUND_ERROR);
        }

        return content;
    }

    @Override
    @Transactional
    public void delete(UUID deleteUuid) {

        if (1 != contentDAO.delete(deleteUuid)) {
            throw contentException(ErrorCodes.CONTENT_DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    public Content update(Content content) {
        if(1 != contentDAO.update(content)) {
            throw contentException(ErrorCodes.CONTENT_UPDATE_ERROR);
        }

        return content;
    }

    private ContentException contentException(ErrorCodes errorCode) {
        return new ContentException(errorCode.name(), messageSourceAccessor.getMessage(errorCode));
    }
}
