package com.tesco.pma.organisation.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.organisation.api.OrganisationDictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganisationDictionaryDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/organisation/dao/";
    private static final String UK_CODE = "uk";

    @Autowired
    private OrganisationDictionaryDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "organisation-dictionary-init.xml"})
    void findOrganisationDictionary() {
        var result = dao.findOrganisationDictionary(UK_CODE);

        assertNotNull(result);
        assertEquals(UK_CODE, result.getCode());
        assertEquals("UK", result.getName());
        assertEquals(1, result.getLevel());

        result = dao.findOrganisationDictionary("invalid_code");

        assertNull(result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "organisation-dictionary-init.xml"})
    void findAllOrganisationDictionaries() {
        var result = dao.findAllOrganisationDictionaries();

        assertEquals(4, result.size());
    }

    @Test
    void create() {
        var code = "code";
        var name = "name";
        var level = 2;
        var od = new OrganisationDictionary();
        od.setCode(code);
        od.setName(name);
        od.setLevel(level);

        var i = dao.create(od);
        assertEquals(1, i);

        var result = dao.findOrganisationDictionary(code);

        assertEquals(code, result.getCode());
        assertEquals(name, result.getName());
        assertEquals(level, result.getLevel());
    }

    @Test
    void update() {
        var code = "salaried";
        var name = "name";
        var level = 2;

        var od = new OrganisationDictionary();
        od.setCode(code);
        od.setName(name);
        od.setLevel(level);

        var i = dao.update(od);
        assertEquals(1, i);

        var result = dao.findOrganisationDictionary(code);

        assertEquals(code, result.getCode());
        assertEquals(name, result.getName());
        assertEquals(level, result.getLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "organisation-dictionary-init.xml"})
    void delete() {
        var result = dao.findOrganisationDictionary(UK_CODE);

        assertNotNull(result);

        var delete = dao.delete(UK_CODE);

        assertEquals(1, delete);

        result = dao.findOrganisationDictionary(UK_CODE);

        assertNull(result);
    }
}
