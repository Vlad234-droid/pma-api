package com.tesco.pma.cms.dao;

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
}
