package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.ConditionGroup;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";
    public static final UUID COLLEAGUE_UUID = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");
    private static final UUID COLLEAGUE_UUID_1 = UUID.fromString("119e0d2b-1dc2-409f-8198-ecd66e59d47a");
    private static final UUID MANAGER_UUID_1 = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");
    private static final UUID MANAGER_UUID_2 = UUID.fromString("b5f79bef-7905-400f-8cb6-d0ebe41b961c");
    private static final String LEGAL_ENTITY = "Tesco Stores Limited";
    private static final String LOCATION_ID = "INDH000001";
    private static final String V_DOW = "Dow";
    private static final String V_JOHN = "John";
    private static final String F_FIRST_NAME_EQ = "first-name_eq";
    private static final String F_LAST_NAME_EQ = "last-name_eq";

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
        assertEquals(COLLEAGUE_UUID, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueByIamIdWithNullWorkLevel() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000001");

        var colleague = dao.getColleagueByIamId("TPX13");

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueByIamIdWithNullDepartment() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000002");

        var colleague = dao.getColleagueByIamId("TPX14");

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueByIamIdWithNullJob() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000003");

        var colleague = dao.getColleagueByIamId("TPX15");

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleague() {
        var colleague = dao.getColleague(COLLEAGUE_UUID);

        assertNotNull(colleague);
        assertEquals(COLLEAGUE_UUID, colleague.getUuid());
        assertEquals(LEGAL_ENTITY, colleague.getLegalEntity());
        assertEquals(LOCATION_ID, colleague.getLocationId());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getAllColleaguesUuids() {
        var uuids = dao.getAllColleaguesUuids(Set.of(COLLEAGUE_UUID, COLLEAGUE_UUID_1, UUID.randomUUID()));

        assertEquals(2, uuids.size());
        assertTrue(uuids.contains(COLLEAGUE_UUID));
        assertTrue(uuids.contains(COLLEAGUE_UUID_1));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueByWL() {
        var rq = new RequestQuery();
        rq.addFilters("work-level_eq", WorkLevel.WL1.name());

        var colleagues = dao.findColleagueSuggestionsByFullName(rq);

        assertEquals(5, colleagues.size());

        var colleaguesIds = colleagues.stream()
                .map(Colleague::getColleagueUUID)
                .map(UUID::toString)
                .collect(Collectors.toSet());

        assertTrue(colleaguesIds.contains(COLLEAGUE_UUID_1.toString()));
        assertTrue(colleaguesIds.contains("45fd1870-9745-41c0-90b5-a902cfca6961"));
        assertTrue(colleaguesIds.contains("b5c74f42-665d-4c9e-9b00-2682eaabe592"));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getSubordinatesTest() {

        dao.updateColleagueManager(COLLEAGUE_UUID_1, MANAGER_UUID_1);
        dao.updateColleagueManager(UUID.fromString("03121555-7246-4b03-b2ed-718cf81d4d31"), MANAGER_UUID_1);

        var rq = new RequestQuery();
        rq.addFilters("manager-uuid_eq", MANAGER_UUID_1);

        var colleagues = dao.findColleagueSuggestionsByFullName(rq);

        assertEquals(2, colleagues.size());

        var colleaguesIds = colleagues.stream()
                .map(Colleague::getColleagueUUID)
                .map(UUID::toString)
                .collect(Collectors.toSet());

        assertTrue(colleaguesIds.contains(COLLEAGUE_UUID_1.toString()));
        assertTrue(colleaguesIds.contains("03121555-7246-4b03-b2ed-718cf81d4d31"));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueWithNullWorkLevel() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000001");

        var colleague = dao.getColleague(colleagueUuid);

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueWithNullDepartment() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000002");

        var colleague = dao.getColleague(colleagueUuid);

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void getColleagueWithNullJob() {
        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000003");

        var colleague = dao.getColleague(colleagueUuid);

        assertNotNull(colleague);
        assertEquals(colleagueUuid, colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void isColleagueExists() {
        var exists = dao.isColleagueExists(COLLEAGUE_UUID);
        assertTrue(exists);

        var notExist = dao.isColleagueExists(UUID.fromString("c409869b-1111-2222-3333-e13af2e6f935"));
        assertFalse(notExist);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void updateColleagueManager() {

        var managerUuid = dao.getColleague(COLLEAGUE_UUID).getManagerUuid();
        assertNotEquals(COLLEAGUE_UUID, managerUuid);

        dao.updateColleagueManager(COLLEAGUE_UUID, COLLEAGUE_UUID);

        managerUuid = dao.getColleague(COLLEAGUE_UUID).getManagerUuid();
        assertEquals(COLLEAGUE_UUID, managerUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void saveColleagueData() {
        var colleagueUuid = UUID.randomUUID();
        var colleague = buildColleagueEntity(colleagueUuid, MANAGER_UUID_2);

        dao.updateCountry(colleague.getCountry());
        dao.updateDepartment(colleague.getDepartment());
        dao.updateJob(colleague.getJob());
        dao.updateWorkLevel(colleague.getWorkLevel());

        dao.saveColleague(colleague);

        var saved = dao.getColleague(colleagueUuid);

        assertEquals(colleagueUuid, saved.getUuid());
        assertEquals(MANAGER_UUID_2, saved.getManagerUuid());
        assertEquals("FN", saved.getFirstName());
        assertEquals("LN", saved.getLastName());
        assertEquals("email", saved.getEmail());
        assertEquals(colleague.getWorkLevel(), saved.getWorkLevel());
        assertEquals("PE", saved.getPrimaryEntity());
        assertEquals(colleague.getCountry(), saved.getCountry());
        assertEquals(colleague.getDepartment(), saved.getDepartment());
        assertEquals("SF", saved.getSalaryFrequency());
        assertEquals(colleague.getJob(), saved.getJob());
        assertEquals("IAM_S", saved.getIamSource());
        assertEquals("IAM_ID", saved.getIamId());
        assertEquals("ET", saved.getEmploymentType());
        assertEquals(LEGAL_ENTITY, saved.getLegalEntity());
        assertEquals(LOCATION_ID, saved.getLocationId());
        assertTrue(saved.isManager());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void updateColleagueData() {
        var colleagueUuid = UUID.randomUUID();
        var colleague = buildColleagueEntity(colleagueUuid, MANAGER_UUID_2);

        dao.updateCountry(colleague.getCountry());
        dao.updateDepartment(colleague.getDepartment());
        dao.updateJob(colleague.getJob());
        dao.updateWorkLevel(colleague.getWorkLevel());

        dao.saveColleague(colleague);

        colleague.setFirstName("FN_1");
        colleague.setLastName("LN_1");
        colleague.setEmail("EMAIL_1");
        colleague.setEmploymentType("ET_1");
        colleague.setSalaryFrequency("SF_1");
        colleague.setPrimaryEntity("PE_1");
        colleague.setManager(false);
        dao.updateColleague(colleague);

        var updated = dao.getColleague(colleagueUuid);

        assertEquals(colleagueUuid, updated.getUuid());
        assertEquals(MANAGER_UUID_2, updated.getManagerUuid());
        assertEquals("FN_1", updated.getFirstName());
        assertEquals("LN_1", updated.getLastName());
        assertEquals("EMAIL_1", updated.getEmail());
        assertEquals("PE_1", updated.getPrimaryEntity());
        assertEquals("SF_1", updated.getSalaryFrequency());
        assertEquals("IAM_S", updated.getIamSource());
        assertEquals("IAM_ID", updated.getIamId());
        assertEquals("ET_1", updated.getEmploymentType());
        assertFalse(updated.isManager());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void updateColleagueSucceeded() {
        assertEquals(1, dao.updateColleague(getCorrectColleague()));
    }

    @Test
    void updateColleagueThrowDataIntegrityViolationException() {
        var colleague = getIncorrectColleague();

        var exception = assertThrows(DataIntegrityViolationException.class, () -> dao.updateColleague(colleague));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("ERROR: insert or update on table \"colleague\" violates foreign key constraint"));
    }

    @Test
    void insertJobWithExistsId() {
        assertEquals(1, dao.updateJob(getJob("1")));
    }

    @Test
    void insertJobWithNotExistsId() {
        assertEquals(1, dao.updateJob(getJob("3")));
    }

    @Test
    void insertCountryWithExistsCode() {
        assertEquals(1, dao.updateCountry(getCountry("GB")));
    }

    @Test
    void insertCountryWithNotExistsCode() {
        assertEquals(1, dao.updateCountry(getCountry("ZZ")));
    }

    @Test
    void insertWorkLevelWithExistsCode() {
        assertEquals(1, dao.updateWorkLevel(getWorkLevel("WL1")));
    }

    @Test
    void insertWorkLevelWithNotExistsCode() {
        assertEquals(1, dao.updateWorkLevel(getWorkLevel("WL7")));
    }

    @Test
    void insertDepartmentWithExistsId() {
        assertEquals(1, dao.updateDepartment(getDepartment("1")));
    }

    @Test
    void insertDepartmentWithNotExistsId() {
        assertEquals(1, dao.updateDepartment(getDepartment("5")));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues.xml"})
    void findColleagueSuggestionsByFullName() {

        dao.updateColleagueManager(COLLEAGUE_UUID_1, MANAGER_UUID_1);

        assertEquals(8, dao.findColleagueSuggestionsByFullName(
                createRQ(Map.of("first-name_like", "fiRst"))).size());

        var colleagues = dao.findColleagueSuggestionsByFullName(
                createRQ(Map.of(
                        "last-name_like", V_DOW,
                        "manager-uuid_eq", MANAGER_UUID_1.toString())));

        assertEquals(1, colleagues.size());
        assertEquals(1, dao.findColleagueSuggestionsByFullName(createRQ(Map.of("first-name_like", "ohn"))).size());
        assertEquals(1, dao.findColleagueSuggestionsByFullName(createRQ(Map.of("last-name_like", "Smith"))).size());

        var colleague = dao.findColleagueSuggestionsByFullName(createRQ(Map.of(
                F_FIRST_NAME_EQ, V_JOHN,
                F_LAST_NAME_EQ, V_DOW
        ))).stream().filter(col -> COLLEAGUE_UUID_1.toString().equals(col.getColleagueUUID().toString())).findFirst().get();

        assertNotNull(colleague);
        assertEquals("Tesco Bank", colleague.getWorkRelationships().get(0).getPrimaryEntity());
        assertEquals(WorkLevel.WL1, colleague.getWorkRelationships().get(0).getWorkLevel());
        assertEquals("2", colleague.getWorkRelationships().get(0).getJob().getId());
        assertEquals("ANNUAL", colleague.getWorkRelationships().get(0).getSalaryFrequency());
        assertEquals("ET", colleague.getWorkRelationships().get(0).getEmploymentType());
        assertEquals(MANAGER_UUID_1.toString(), colleague.getWorkRelationships().get(0).getManagerUUID().toString());
        assertEquals("4", colleague.getWorkRelationships().get(0).getDepartment().getId());
        assertEquals(V_JOHN, colleague.getProfile().getFirstName());
        assertEquals(V_DOW, colleague.getProfile().getLastName());
        assertEquals("Michael", colleague.getProfile().getMiddleName());
        assertEquals("test@test", colleague.getContact().getEmail());
        assertEquals("TPX2", colleague.getExternalSystems().getIam().getId());
        assertEquals("Test", colleague.getExternalSystems().getIam().getSource());
        assertNotNull(colleague.getServiceDates().getHireDate());
        assertNotNull(colleague.getServiceDates().getLeavingDate());

    }

    @Test
    void findColleagueSuggestionsGroupByAnd() {
        var group = new ConditionGroup();
        group.addFilters(F_FIRST_NAME_EQ, V_JOHN);
        group.addFilters(F_LAST_NAME_EQ, V_DOW);

        var rq = new RequestQuery();
        rq.setGroups(List.of(group));


        assertEquals(1, dao.findColleagueSuggestionsByFullName(rq).size());
    }

    private RequestQuery createRQ(Map<String, Object> filters) {
        var result = new RequestQuery();
        filters.forEach(result::addFilters);
        return result;
    }

    private ColleagueEntity getCorrectColleague() {
        var colleague = new ColleagueEntity();
        colleague.setUuid(COLLEAGUE_UUID_1);
        colleague.setManager(false);
        colleague.setManagerUuid(MANAGER_UUID_1);

        colleague.setJob(getJob("1"));
        colleague.setCountry(getCountry("GB"));
        colleague.setWorkLevel(getWorkLevel("WL1"));
        colleague.setDepartment(getDepartment("1"));

        return colleague;
    }

    private ColleagueEntity getIncorrectColleague() {
        var colleague = new ColleagueEntity();
        colleague.setUuid(COLLEAGUE_UUID_1);
        colleague.setManager(false);
        colleague.setManagerUuid(MANAGER_UUID_1);

        colleague.setJob(getJob("3"));
        colleague.setCountry(getCountry("DE"));
        colleague.setWorkLevel(getWorkLevel("WL7"));
        colleague.setDepartment(getDepartment("5"));

        return colleague;
    }

    private ColleagueEntity.Job getJob(String id) {
        ColleagueEntity.Job job = new ColleagueEntity.Job();
        job.setId(id);
        job.setCode("TL" + id);
        job.setName("Team lead" + id);
        job.setCostCategory("cc" + id);
        return job;
    }

    private ColleagueEntity.Country getCountry(String code) {
        ColleagueEntity.Country country = new ColleagueEntity.Country();
        country.setCode(code);
        country.setName(code);
        return country;
    }

    private ColleagueEntity.WorkLevel getWorkLevel(String code) {
        ColleagueEntity.WorkLevel workLevel = new ColleagueEntity.WorkLevel();
        workLevel.setCode(code);
        workLevel.setName(code);
        return workLevel;
    }

    private ColleagueEntity.Department getDepartment(String id) {
        ColleagueEntity.Department department = new ColleagueEntity.Department();
        department.setId(id);
        department.setName(id);
        department.setBusinessType(id);
        return department;
    }

    private ColleagueEntity buildColleagueEntity(UUID colleagueUuid, UUID managerUuid) {
        var job = new ColleagueEntity.Job();
        job.setId("JobID");
        job.setCode("JC");

        var country = new ColleagueEntity.Country();
        country.setCode("US");

        var workLevel = new ColleagueEntity.WorkLevel();
        workLevel.setCode("WL1_new");

        var dep = new ColleagueEntity.Department();
        dep.setId("DepId");
        dep.setBusinessType("Store");

        var colleague = new ColleagueEntity();
        colleague.setUuid(colleagueUuid);
        colleague.setManagerUuid(managerUuid);
        colleague.setFirstName("FN");
        colleague.setLastName("LN");
        colleague.setEmail("email");
        colleague.setWorkLevel(workLevel);
        colleague.setPrimaryEntity("PE");
        colleague.setCountry(country);
        colleague.setDepartment(dep);
        colleague.setSalaryFrequency("SF");
        colleague.setJob(job);
        colleague.setIamSource("IAM_S");
        colleague.setIamId("IAM_ID");
        colleague.setManager(true);
        colleague.setEmploymentType("ET");
        colleague.setLocationId(LOCATION_ID);
        colleague.setLegalEntity(LEGAL_ENTITY);

        return colleague;
    }

}
