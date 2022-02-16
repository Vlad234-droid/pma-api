package com.tesco.pma.cms.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.MapJson;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentEntryDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cms/dao/";
    private static final String TEST_CONTENT_UUID = "d9d819fc-c1ee-4df8-a87b-d88f1c006c11";
    private static final String TEST_CONTENT_UUID_2 = "a49bee82-71bb-4cad-b698-24422fb2fb29";
    private static final String KEY = "knowledge-library/gb/content";
    private static final String KEY_EQ = "key_eq";

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
        var uuid = UUID.randomUUID();
        var content = new ContentEntry();
        content.setUuid(uuid);
        content.setStatus(ContentStatus.DRAFT);
        content.setTitle("test3");
        content.setKey("some/key");
        content.setCreatedBy(UUID.randomUUID());
        content.setCreatedTime(Instant.now());
        content.setVersion(1);

        var mapJson = new MapJson();
        mapJson.setMapJson(Map.of("test", "test_val"));
        content.setProperties(mapJson);

        assertEquals(1, contentEntryDAO.create(content));

        var contentFetched = contentEntryDAO.find(RequestQuery.create("uuid_eq", uuid)).get(0);

        assertEquals("test_val", contentFetched.getProperties().getMapJson().get("test"));
    }

    @Test
    void deleteTest(){
        contentEntryDAO.delete(UUID.fromString(TEST_CONTENT_UUID));

        assertEquals(0,
                contentEntryDAO.find(RequestQuery.create("uuid_eq", UUID.fromString(TEST_CONTENT_UUID))).size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findByKeyTest() {
        assertEquals(3, contentEntryDAO.find(RequestQuery.create(KEY_EQ, KEY)).size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void updateTest() {
        var content = contentEntryDAO.find(RequestQuery.create("uuid_eq", UUID.fromString(TEST_CONTENT_UUID))).get(0);
        content.setStatus(ContentStatus.UNPUBLISHED);

        contentEntryDAO.update(content);

        content = contentEntryDAO.find(RequestQuery.create("uuid_eq", UUID.fromString(TEST_CONTENT_UUID))).get(0);
        assertEquals(ContentStatus.UNPUBLISHED, content.getStatus());
        assertEquals(3, content.getVersion());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findTest() {
        var rq = new RequestQuery();
        rq.addFilters("status_eq", "pUbLished");
        rq.addFilters(KEY_EQ, KEY);

        assertEquals(3, contentEntryDAO.find(rq).size());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void propertiesTest() {

        var content =
                contentEntryDAO.find(RequestQuery.create("uuid_eq", UUID.fromString(TEST_CONTENT_UUID))).get(0);

        assertEquals("link/test", content.getProperties().getMapJson().get("image_link"));

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findLatestVersionTest() {
        var rq = new RequestQuery();
        rq.addFilters("title_eq", "test");
        rq.addFilters(KEY_EQ, KEY);
        rq.addFilters("version_eq", -1);

        List<ContentEntry> contentList = contentEntryDAO.find(rq);
        assertEquals(1, contentList.size());
        assertEquals(TEST_CONTENT_UUID_2, contentEntryDAO.find(rq).get(0).getUuid().toString());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findByVersionTest() {
        var rq = new RequestQuery();
        rq.addFilters("title_eq", "test");
        rq.addFilters(KEY_EQ, KEY);
        rq.addFilters("version_eq", 1);

        assertEquals(1, contentEntryDAO.find(rq).size());
        assertEquals(TEST_CONTENT_UUID, contentEntryDAO.find(rq).get(0).getUuid().toString());

    }

}
