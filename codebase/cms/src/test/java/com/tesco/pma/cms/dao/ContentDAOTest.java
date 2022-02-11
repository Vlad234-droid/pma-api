package com.tesco.pma.cms.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.cms.controller.dto.Key;
import com.tesco.pma.cms.model.Content;
import com.tesco.pma.cms.model.ContentStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cms/dao/";

    @Autowired
    private ContentDAO contentDAO;


    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void createTest(){
        var content = new Content();
        content.setId(UUID.randomUUID());
        content.setStatus(ContentStatus.DRAFT);
        content.setKey("some/key");
        content.setImageLink("image/link");
        content.setCreatedBy(UUID.randomUUID());
        content.setCreatedTime(Instant.now());
        content.setVersion(1);

        assertEquals(1, contentDAO.create(content));
    }

    @Test
    void deleteTest(){
        contentDAO.delete(UUID.fromString("d9d819fc-c1ee-4df8-a87b-d88f1c006c11"));

        assertEquals(0, contentDAO.findByKey("knowledge-library/gb/content").size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "contents.xml"})
    void findByKeyTest() {
        assertEquals(1, contentDAO.findByKey("knowledge-library/gb/content").size());
    }


}
