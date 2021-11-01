package com.tesco.pma.organisation.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigEntryTypeDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/organisation/dao/";
    private static final UUID CE_UUID = UUID.fromString("dc55e38f-d4cc-4420-b20c-d9fcfed8ba40");
    private static final UUID CE_UUID_2 = UUID.fromString("a7a76484-bbe2-4b61-b6f6-ea260159a340");
    private static final String COMPOSITE_KEY_FILTER = "BU/WCE%/#v2";

    @Autowired
    private ConfigEntryTypeDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findConfigEntryType() {
        final var result = dao.findConfigEntryType(1);

        assertEquals(1, result.getId());
        assertEquals("BU", result.getCode());
        assertEquals("bu desc", result.getDescription());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findAllConfigEntryTypes() {
        final var result = dao.findAllConfigEntryTypes();

        assertEquals(2, result.size());
    }
}
