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

        var invalidOrganisationDictionary = dao.findOrganisationDictionary("invalid_code");

        assertNull(invalidOrganisationDictionary);
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

        var result = dao.create(od);
        assertEquals(1, result);

        var organisationDictionary = dao.findOrganisationDictionary(code);

        assertEquals(code, organisationDictionary.getCode());
        assertEquals(name, organisationDictionary.getName());
        assertEquals(level, organisationDictionary.getLevel());
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

        var result = dao.update(od);
        assertEquals(1, result);

        var organisationDictionary = dao.findOrganisationDictionary(code);

        assertEquals(code, organisationDictionary.getCode());
        assertEquals(name, organisationDictionary.getName());
        assertEquals(level, organisationDictionary.getLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "organisation-dictionary-init.xml"})
    void delete() {
        var organisationDictionary = dao.findOrganisationDictionary(UK_CODE);

        assertNotNull(organisationDictionary);

        var result = dao.delete(UK_CODE);

        assertEquals(1, result);

        organisationDictionary = dao.findOrganisationDictionary(UK_CODE);

        assertNull(organisationDictionary);
    }
}
