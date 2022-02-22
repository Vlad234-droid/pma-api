package com.tesco.pma.cms.service;

import com.tesco.pma.cms.dao.ContentEntryDAO;
import com.tesco.pma.cms.exception.CMSException;
import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.cms.model.ContentStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContentEntryServiceTest {

    private final ContentEntryDAO contentEntryDAO = Mockito.mock(ContentEntryDAO.class);
    private final AuditorAware<UUID> auditorAware = Mockito.mock(AuditorAware.class);
    private final NamedMessageSourceAccessor messageSourceAccessor = Mockito.mock(NamedMessageSourceAccessor.class);
    private final ContentEntryServiceImpl contentEntryService = new ContentEntryServiceImpl(contentEntryDAO, auditorAware, messageSourceAccessor);

    @Test
    void createTest(){

        var newContent = new ContentEntry();
        newContent.setTitle("test");

        Mockito.when(contentEntryDAO.create(Mockito.eq(newContent))).thenReturn(1);

        contentEntryService.create(newContent);

        assertEquals(ContentStatus.DRAFT, newContent.getStatus());
    }

    @Test()
    void createWithoutTitleTest(){

        assertThrows(CMSException.class, () -> {
            var newContent = new ContentEntry();
            Mockito.when(contentEntryDAO.create(Mockito.eq(newContent))).thenReturn(1);

            contentEntryService.create(newContent);
        });
    }


}
