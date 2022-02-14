package com.tesco.pma.cms.service;


import com.tesco.pma.cms.dao.ContentEntryDAO;
import com.tesco.pma.cms.exception.ContentException;
import com.tesco.pma.cms.exception.ErrorCodes;
import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.cms.model.ContentStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentEntryDAO contentEntryDAO;
    private final AuditorAware<UUID> auditorAware;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public ContentEntry create(ContentEntry contentEntry) {
        contentEntry.setId(UUID.randomUUID());
        contentEntry.setCreatedBy(auditorAware.getCurrentAuditor());
        contentEntry.setCreatedTime(Instant.now());

        if (contentEntry.getVersion() == null) {
            contentEntry.setVersion(1);
        }

        if (contentEntry.getStatus() == null) {
            contentEntry.setStatus(ContentStatus.PUBLISHED);
        }

        if (1 != contentEntryDAO.create(contentEntry)) {
            throw contentException(ErrorCodes.CONTENT_CREATE_ERROR);
        }

        return contentEntry;
    }

    @Override
    public List<ContentEntry> findByKey(String key) {
        return contentEntryDAO.findByKey(key);
    }

    @Override
    public List<ContentEntry> findByRequestQuery(RequestQuery rq) {
        return contentEntryDAO.find(rq);
    }

    @Override
    public ContentEntry findById(UUID uuid) {
        var content = contentEntryDAO.findById(uuid);

        if (content == null) {
            throw contentException(ErrorCodes.CONTENT_NOT_FOUND_ERROR);
        }

        return content;
    }

    @Override
    @Transactional
    public void delete(UUID deleteUuid) {

        if (1 != contentEntryDAO.delete(deleteUuid)) {
            throw contentException(ErrorCodes.CONTENT_DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    public ContentEntry update(ContentEntry contentEntry) {
        if (1 != contentEntryDAO.update(contentEntry)) {
            throw contentException(ErrorCodes.CONTENT_UPDATE_ERROR);
        }

        return contentEntry;
    }

    private ContentException contentException(ErrorCodes errorCode) {
        return new ContentException(errorCode.name(), messageSourceAccessor.getMessage(errorCode));
    }
}
