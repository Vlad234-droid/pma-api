package com.tesco.pma.organisation.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigEntryDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/organisation/dao/";
    private static final UUID CE_UUID = UUID.fromString("dc55e38f-d4cc-4420-b20c-d9fcfed8ba40");
    private static final UUID CE_UUID_2 = UUID.fromString("a7a76484-bbe2-4b61-b6f6-ea260159a340");
    private static final UUID CE_UUID_3 = UUID.fromString("6bc4f35b-4fd2-4e95-986e-765e4fd9b037");
    private static final String COMPOSITE_KEY_FILTER = "BU/WCE%/#v2";

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
        var cet = new GeneralDictionaryItem();
        cet.setId(1);
        ce.setType(cet);
        ce.setVersion(4);
        ce.setParentUuid(CE_UUID_2);
        ce.setCompositeKey("BU/C22/#v4");

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
        final var result = dao.findPublishedConfigEntriesByKey(COMPOSITE_KEY_FILTER);

        assertThat(result)
                .hasSize(2)
                .element(0)
                .returns(CE_UUID_3, ConfigEntry::getUuid);

        assertThat(result)
                .element(1)
                .returns(UUID.fromString("aedff942-2e19-44e0-9c23-bd8f152a937f"), ConfigEntry::getUuid)
                .returns(CE_UUID_3, ConfigEntry::getParentUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void unpublishConfigEntries() {

        var result = dao.findPublishedConfigEntriesByKey(COMPOSITE_KEY_FILTER);

        assertThat(result).hasSize(2);

        dao.unpublishConfigEntries(COMPOSITE_KEY_FILTER);

        assertThat(dao.findPublishedConfigEntriesByKey(COMPOSITE_KEY_FILTER)).isEmpty();
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void publishConfigEntries() {

        var cet = new GeneralDictionaryItem();
        cet.setId(1);

        var wce = new WorkingConfigEntry();
        wce.setName("WCE3");
        wce.setType(cet);
        wce.setVersion(2);
        wce.setConfigEntryUuid(CE_UUID_2);
        wce.setCompositeKey("BU/WCE/BU/WCE1/BU/WCE3/#v2");

        dao.publishConfigEntry(wce);

        var result = dao.findPublishedConfigEntriesByKey(COMPOSITE_KEY_FILTER);

        assertThat(result).hasSize(3)
                .element(2)
                .returns(CE_UUID_2, ConfigEntry::getUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void getMaxVersionForRootEntry() {

        var result = dao.getMaxVersionForRootEntry("CE1", 1);
        assertEquals(3, result);

        result = dao.getMaxVersionForRootEntry("Invalid name", 1);
        assertEquals(0, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findAllUnpublishedRootEntries() {

        var result = dao.findAllUnpublishedRootEntries();

        assertThat(result).hasSize(3);

        var uuids = result.stream().map(ConfigEntry::getUuid).collect(Collectors.toSet());

        assertTrue(uuids.contains(CE_UUID));
        assertTrue(uuids.contains(UUID.fromString("56141037-6e2d-45f0-b47f-4875e68dd1d7")));
        assertTrue(uuids.contains(CE_UUID_3));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findAllPublishedRootEntries() {

        var result = dao.findAllPublishedRootEntries();

        assertThat(result).singleElement()
                .returns(CE_UUID_3, ConfigEntry::getUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findPublishedConfigEntryChildStructure() {
        final var unpublishedResult = dao.findPublishedConfigEntryChildStructure(CE_UUID);

        assertThat(unpublishedResult).isEmpty();

        final var result = dao.findPublishedConfigEntryChildStructure(CE_UUID_3);

        assertThat(result)
                .hasSize(2)
                .element(1)
                .returns(UUID.fromString("aedff942-2e19-44e0-9c23-bd8f152a937f"), ConfigEntry::getUuid)
                .returns(CE_UUID_3, ConfigEntry::getParentUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void findConfigEntriesByKey() {
        final var result = dao.findConfigEntriesByKey("BU/CE1%/#v3");

        assertThat(result)
                .hasSize(2)
                .element(0)
                .returns(CE_UUID, ConfigEntry::getUuid);

        assertThat(result)
                .element(1)
                .returns(CE_UUID_2, ConfigEntry::getUuid)
                .returns(CE_UUID, ConfigEntry::getParentUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "config_entries_init.xml"})
    void deleteConfigEntry() {
        dao.deleteConfigEntry(CE_UUID);

        assertThat(dao.findConfigEntryChildStructure(CE_UUID))
                .isEmpty();

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues-config.xml"})
    void isColleagueExistsForCompositeKey() {
        var colleagueUuid = UUID.fromString("b5c74f42-665d-4c9e-9b00-2682eaabe592");
        var key = "overall_leadership/group_a/wl4_wl5";

        var exists = dao.isColleagueExistsForCompositeKey(colleagueUuid, key);

        assertTrue(exists);

    }

    @ParameterizedTest
    @MethodSource("provideArgsForGettingColleagues")
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues-config.xml"})
    void readColleaguesByKey(String key, Set<UUID> colleagueUuids) {
        var colleagues = dao.findColleaguesByCompositeKey(key, null);

        assertEquals(colleagueUuids.size(), colleagues.size());

        var uuids = colleagues.stream()
                .map(ColleagueEntity::getUuid)
                .collect(Collectors.toSet());

        assertTrue(colleagueUuids.containsAll(uuids));

    }

    private static Stream<Arguments> provideArgsForGettingColleagues() { //NOPMD
        var e1 = UUID.fromString("b5c74f42-665d-4c9e-9b00-2682eaabe592");
        var e2 = UUID.fromString("71eb5890-4f41-4d70-a3a0-0071fcfd2edb");
        var e3 = UUID.fromString("03121555-7246-4b03-b2ed-718cf81d4d31");
        return Stream.of(
                Arguments.of(
                        "overall_leadership/group_a/wl4_wl5",
                        Set.of(e1, e2)),
                Arguments.of(
                        "overall_leadership/group_a/wl3",
                        Set.of(e3))
        );
    }
}
