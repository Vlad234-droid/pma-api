package com.tesco.pma.cms.service;


import com.tesco.pma.cms.dao.ContentEntryDAO;
import com.tesco.pma.cms.exception.CMSException;
import com.tesco.pma.cms.exception.ErrorCodes;
import com.tesco.pma.cms.api.ContentEntry;
import com.tesco.pma.cms.api.ContentStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentEntryServiceImpl implements ContentEntryService {

    private final ContentEntryDAO contentEntryDAO;
    private final AuditorAware<UUID> auditorAware;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public ContentEntry create(ContentEntry content) {
        content.setUuid(UUID.randomUUID());
        content.setCreatedBy(auditorAware.getCurrentAuditor());
        content.setCreatedTime(Instant.now());

        if (StringUtils.isEmpty(content.getTitle())) {
            throw contentException(ErrorCodes.CONTENT_NO_TITLE_ERROR);
        }

        if (content.getStatus() == null) {
            content.setStatus(ContentStatus.DRAFT);
        }

        if (1 != contentEntryDAO.create(content)) {
            throw contentException(ErrorCodes.CONTENT_CREATE_ERROR);
        }

        return content;
    }

    @Override
    public List<ContentEntry> find(String key) {
        return contentEntryDAO.find(RequestQuery.create("key_eq", key));
    }

    @Override
    public List<ContentEntry> find(RequestQuery rq) {
        return contentEntryDAO.find(rq);
    }

    @Override
    public ContentEntry read(UUID uuid) {

        var content = contentEntryDAO.find(RequestQuery.create("uuid_eq", uuid)).get(0);

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
    public ContentEntry update(ContentEntry content) {

        if (1 != contentEntryDAO.update(content)) {
            throw contentException(ErrorCodes.CONTENT_UPDATE_ERROR);
        }

        return content;
    }

    private CMSException contentException(ErrorCodes errorCode) {
        return new CMSException(errorCode.name(), messageSourceAccessor.getMessage(errorCode));
    }
}
