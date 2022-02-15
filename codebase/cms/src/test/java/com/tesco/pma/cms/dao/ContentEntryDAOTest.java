package com.tesco.pma.cms.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.cms.model.ContentStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ContentEntryDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cms/dao/";
    private static final String TEST_CONTENT_UUID = "d9d819fc-c1ee-4df8-a87b-d88f1c006c11";
    private static final String TEST_CONTENT_UUID_2 = "a49bee82-71bb-4cad-b698-24422fb2fb29";

    @Autowired
    private ContentEntryDAO contentEntryDAO;


    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void createTest(){
        var content = new ContentEntry();
        content.setId(UUID.randomUUID());
        content.setStatus(ContentStatus.DRAFT);
        content.setTitle("test3");
        content.setKey("some/key");
        content.setImageLink("image/link");
        content.setCreatedBy(UUID.randomUUID());
        content.setCreatedTime(Instant.now());
        content.setVersion(1);

        assertEquals(1, contentEntryDAO.create(content));
    }

    @Test
    void deleteTest(){
        contentEntryDAO.delete(UUID.fromString(TEST_CONTENT_UUID));

        assertNull(contentEntryDAO.findById(UUID.fromString(TEST_CONTENT_UUID)));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findByKeyTest() {
        assertEquals(3, contentEntryDAO.findByKey("knowledge-library/gb/content").size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void updateTest() {
        var content = contentEntryDAO.findById(UUID.fromString(TEST_CONTENT_UUID));
        content.setStatus(ContentStatus.UNPUBLISHED);

        contentEntryDAO.update(content);

        content = contentEntryDAO.findById(UUID.fromString(TEST_CONTENT_UUID));
        assertEquals(ContentStatus.UNPUBLISHED, content.getStatus());
        assertEquals(3, content.getVersion());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findTest() {
        var rq = new RequestQuery();
        rq.addFilters("status_eq", "pUbLished");
        rq.addFilters("key_eq", "knowledge-library/gb/content");

        assertEquals(3, contentEntryDAO.find(rq).size());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findLatestVersionTest() {
        var rq = new RequestQuery();
        rq.addFilters("title_eq", "test");
        rq.addFilters("key_eq", "knowledge-library/gb/content");
        rq.addFilters("version_eq", -1);

        List<ContentEntry> contentList = contentEntryDAO.find(rq);
        assertEquals(1, contentList.size());
        assertEquals(TEST_CONTENT_UUID_2, contentEntryDAO.find(rq).get(0).getId().toString());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findByVersionTest() {
        var rq = new RequestQuery();
        rq.addFilters("title_eq", "test");
        rq.addFilters("key_eq", "knowledge-library/gb/content");
        rq.addFilters("version_eq", 1);

        assertEquals(1, contentEntryDAO.find(rq).size());
        assertEquals(TEST_CONTENT_UUID, contentEntryDAO.find(rq).get(0).getId().toString());

    }

}
