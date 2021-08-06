package com.tesco.pma.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.Subsidiary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource(properties = "logging.level.com.tesco.pma.dao=DEBUG")
@DataSet("com/tesco/pma/dao/subsidiary_init.xml")
class SubsidiaryDAOTest extends AbstractDAOTest {

    private static final UUID SUBSIDIARY_UUID = fromString("5d9bbac9-850a-45e3-856b-50be9b9f563c");
    private static final UUID SUBSIDIARY_UUID_2 = fromString("7fa4616e-16c5-433b-80ed-5c184d0efd05");
    private static final UUID REMOVED_SUBSIDIARY_UUID = fromString("ffd94e97-a5a6-4237-8b51-255e1bf72efe");
    private static final String NAME = "subsidiary name";
    private static final String NAME_2 = "subsidiary name2";

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Autowired
    private SubsidiaryDAO instance; //NOPMD

    @Test
    void insertAndGetById() { //NOPMD
        var subsidiary = new Subsidiary();
        var randomUUID = UUID.randomUUID();
        subsidiary.setUuid(randomUUID);
        subsidiary.setName("test name");

        var rowsInserted = instance.insert(subsidiary);
        var result = instance.get(subsidiary.getUuid());

        assertEquals(1, rowsInserted);
        assertNotNull(result);
        assertEquals(randomUUID, result.getUuid());
        assertEquals("test name", result.getName());
    }

    @Test
    void getAll() { //NOPMD
        var result = instance.getAll();

        assertFalse(result.isEmpty());
        assertEquals(6, result.size());
        assertThat(result).extracting("uuid", "name")
                .contains(tuple(SUBSIDIARY_UUID, NAME),
                        tuple(SUBSIDIARY_UUID_2, NAME_2));
    }


    @Test
    void update() { //NOPMD
        var subsidiaryToUpdate = new Subsidiary();
        subsidiaryToUpdate.setUuid(SUBSIDIARY_UUID);
        subsidiaryToUpdate.setName("subsidiary updated");

        var resultBeforeUpdate = instance.get(SUBSIDIARY_UUID);
        int rowsUpdated = instance.update(subsidiaryToUpdate); //NOPMD
        var resultAfterUpdate = instance.get(SUBSIDIARY_UUID);

        assertEquals(1, rowsUpdated);
        assertNotNull(resultBeforeUpdate);
        assertEquals(SUBSIDIARY_UUID, resultBeforeUpdate.getUuid());
        assertEquals(NAME, resultBeforeUpdate.getName());
        assertNotNull(resultAfterUpdate);
        assertEquals(SUBSIDIARY_UUID, resultAfterUpdate.getUuid());
        assertEquals("subsidiary updated", resultAfterUpdate.getName());
    }

    @Test
    void updateRemovedSubsidiary() { //NOPMD
        var subsidiaryToUpdate = new Subsidiary();
        subsidiaryToUpdate.setUuid(REMOVED_SUBSIDIARY_UUID);
        subsidiaryToUpdate.setName("subsidiary updated");

        int rowsUpdated = instance.update(subsidiaryToUpdate);

        assertEquals(0, rowsUpdated);
    }

    @Test
    void delete() { //NOPMD
        var resultBeforeDelete = instance.get(SUBSIDIARY_UUID);
        int rowsDeleted = instance.deleteSubsidiary(SUBSIDIARY_UUID); //NOPMD
        var resultAfterDelete = instance.get(SUBSIDIARY_UUID);

        assertNotNull(resultBeforeDelete);
        assertEquals(1, rowsDeleted);
        assertEquals(SUBSIDIARY_UUID, resultBeforeDelete.getUuid());
        assertEquals(NAME, resultBeforeDelete.getName());
        assertEquals(null, resultAfterDelete);
    }
}
