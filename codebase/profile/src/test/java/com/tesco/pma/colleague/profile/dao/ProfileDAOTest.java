package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";

    @Autowired
    private ProfileDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueByIamId() {
        var colleague = dao.getColleagueByIamId("TPX1");

        assertNotNull(colleague);
        assertEquals(UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935"), colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }
}
