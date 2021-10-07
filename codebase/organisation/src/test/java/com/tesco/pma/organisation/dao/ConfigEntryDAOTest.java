package com.tesco.pma.organisation.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryType;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigEntryDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/organisation/dao/";
    private static final UUID CE_UUID = UUID.fromString("dc55e38f-d4cc-4420-b20c-d9fcfed8ba40");
    private static final UUID CE_UUID_2 = UUID.fromString("a7a76484-bbe2-4b61-b6f6-ea260159a340");

    @Autowired
    private ConfigEntryDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findRootConfigEntry() {
        final var result = dao.findRootConfigEntry(CE_UUID_2);

        assertNotNull(result);
        assertEquals(CE_UUID, result.getUuid());
        assertNull(result.getParentUuid());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findConfigEntryParentStructure() {
        final var result = dao.findConfigEntryParentStructure(CE_UUID_2);

        assertThat(result)
                .hasSize(2)
                .element(0)
                .returns(CE_UUID, ConfigEntry::getUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findConfigEntryChildStructure() {
        final var result = dao.findConfigEntryChildStructure(CE_UUID);

        assertThat(result)
                .hasSize(2)
                .element(1)
                .returns(CE_UUID_2, ConfigEntry::getUuid)
                .returns(CE_UUID, ConfigEntry::getParentUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void create() {
        var result = dao.findConfigEntryChildStructure(CE_UUID);

        assertThat(result).hasSize(2);

        var ce = new ConfigEntry();
        var uuid = UUID.fromString("fe33d24d-1fd2-4e68-8dff-6220609a80df");
        ce.setUuid(uuid);
        ce.setName("C22");
        var cet = new ConfigEntryType();
        cet.setId(1);
        ce.setType(cet);
        ce.setVersion(4);
        ce.setParentUuid(CE_UUID_2);

        var configEntry = dao.createConfigEntry(ce);

        assertEquals(1, configEntry);

        result = dao.findConfigEntryChildStructure(CE_UUID);

        assertThat(result).hasSize(3)
                .element(2)
                .returns(uuid, ConfigEntry::getUuid)
                .returns(4, ConfigEntry::getVersion)
                .returns(CE_UUID_2, ConfigEntry::getParentUuid);

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findPublishedConfigEntriesByKey() {
        final var result = dao.findPublishedConfigEntriesByKey("BU/WCE%/#v2");

        assertThat(result)
                .hasSize(2)
                .element(0)
                .returns(UUID.fromString("6bc4f35b-4fd2-4e95-986e-765e4fd9b037"), ConfigEntry::getUuid);

        assertThat(result)
                .element(1)
                .returns(UUID.fromString("aedff942-2e19-44e0-9c23-bd8f152a937f"), ConfigEntry::getUuid)
                .returns(UUID.fromString("6bc4f35b-4fd2-4e95-986e-765e4fd9b037"), ConfigEntry::getParentUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void unpublishConfigEntries() {

        var result = dao.findPublishedConfigEntriesByKey("BU/WCE%/#v2");

        assertThat(result).hasSize(2);

        dao.unpublishConfigEntries("BU/WCE%/#v2");

        assertThat(dao.findPublishedConfigEntriesByKey("BU/WCE%/#v2")).isEmpty();
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void publishConfigEntries() {

        var cet = new ConfigEntryType();
        cet.setId(1);

        var wce = new WorkingConfigEntry();
        wce.setName("WCE3");
        wce.setType(cet);
        wce.setVersion(2);
        wce.setConfigEntryUuid(CE_UUID_2);
        wce.setCompositeKey("BU/WCE/BU/WCE1/BU/WCE3/#v2");

        dao.publishConfigEntry(wce);

        var result = dao.findPublishedConfigEntriesByKey("BU/WCE%/#v2");

        assertThat(result).hasSize(3)
                .element(2)
                .returns(CE_UUID_2, ConfigEntry::getUuid);
    }
}
