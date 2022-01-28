package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";
    public static final UUID COLLEAGUE_UUID = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");
    private static final UUID COLLEAGUE_UUID_1 = UUID.fromString("119e0d2b-1dc2-409f-8198-ecd66e59d47a");
    private static final UUID MANAGER_UUID_1 = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");

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
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
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
    void saveColleagueData() { //NOPMD
        var colleagueUuid = UUID.randomUUID();
        var managerUuid = UUID.fromString("b5f79bef-7905-400f-8cb6-d0ebe41b961c");

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

        dao.updateCountry(country);
        dao.updateDepartment(dep);
        dao.updateJob(job);
        dao.updateWorkLevel(workLevel);

        dao.saveColleague(colleague);

        var saved = dao.getColleague(colleagueUuid);

        assertEquals(colleagueUuid, saved.getUuid());
        assertEquals(managerUuid, saved.getManagerUuid());
        assertEquals("FN", saved.getFirstName());
        assertEquals("LN", saved.getLastName());
        assertEquals("email", saved.getEmail());
        assertEquals(workLevel, saved.getWorkLevel());
        assertEquals("PE", saved.getPrimaryEntity());
        assertEquals(country, saved.getCountry());
        assertEquals(dep, saved.getDepartment());
        assertEquals("SF", saved.getSalaryFrequency());
        assertEquals(job, saved.getJob());
        assertEquals("IAM_S", saved.getIamSource());
        assertEquals("IAM_ID", saved.getIamId());
        assertEquals("ET", saved.getEmploymentType());
        assertTrue(saved.isManager());

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
        assertEquals(managerUuid, updated.getManagerUuid());
        assertEquals("FN_1", updated.getFirstName());
        assertEquals("LN_1", updated.getLastName());
        assertEquals("EMAIL_1", updated.getEmail());
        assertEquals(workLevel, updated.getWorkLevel());
        assertEquals("PE_1", updated.getPrimaryEntity());
        assertEquals(country, updated.getCountry());
        assertEquals(dep, updated.getDepartment());
        assertEquals("SF_1", updated.getSalaryFrequency());
        assertEquals(job, updated.getJob());
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

        var managerUUID = "c409869b-2acf-45cd-8cc6-e13af2e6f935";

        dao.updateColleagueManager(UUID.fromString("119e0d2b-1dc2-409f-8198-ecd66e59d47a"),
                UUID.fromString(managerUUID));

        assertEquals(8, dao.findColleagueSuggestionsByFullName(
                createRQ(Map.of("first-name_like", "fiRst"))).size());

        var colleagues = dao.findColleagueSuggestionsByFullName(
                createRQ(Map.of(
                        "last-name_like", "Dow",
                        "manager-uuid_eq", managerUUID)));

        assertEquals(1, colleagues.size());
        assertEquals(1, dao.findColleagueSuggestionsByFullName(createRQ(Map.of("first-name_like","ohn"))).size());
        assertEquals(1, dao.findColleagueSuggestionsByFullName(createRQ(Map.of("last-name_like","Smith"))).size());

        var colleague = dao.findColleagueSuggestionsByFullName(createRQ(Map.of(
                "first-name_eq", "John",
                "last-name_eq", "Dow"
        ))).stream().filter(col -> "119e0d2b-1dc2-409f-8198-ecd66e59d47a".equals(col.getColleagueUUID().toString())).findFirst().get();

        assertNotNull(colleague);
        assertEquals("Tesco Bank", colleague.getWorkRelationships().get(0).getPrimaryEntity());
        assertEquals(WorkLevel.WL1, colleague.getWorkRelationships().get(0).getWorkLevel());
        assertEquals("2", colleague.getWorkRelationships().get(0).getJob().getId());
        assertEquals("ANNUAL", colleague.getWorkRelationships().get(0).getSalaryFrequency());
        assertEquals("ET", colleague.getWorkRelationships().get(0).getEmploymentType());
        assertEquals(managerUUID, colleague.getWorkRelationships().get(0).getManagerUUID().toString());
        assertEquals("4", colleague.getWorkRelationships().get(0).getDepartment().getId());
        assertEquals("John", colleague.getProfile().getFirstName());
        assertEquals("Dow", colleague.getProfile().getLastName());
        assertEquals("Michael", colleague.getProfile().getMiddleName());
        assertEquals("test@test", colleague.getContact().getEmail());
        assertEquals("TPX2", colleague.getExternalSystems().getIam().getId());
        assertEquals("Test", colleague.getExternalSystems().getIam().getSource());
        assertNotNull(colleague.getServiceDates().getHireDate());
        assertNotNull(colleague.getServiceDates().getLeavingDate());

    }

    private RequestQuery createRQ(Map<String, Object> filters){
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

}
