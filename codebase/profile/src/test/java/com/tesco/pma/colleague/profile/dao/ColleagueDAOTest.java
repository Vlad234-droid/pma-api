package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataSet({"com/tesco/pma/colleague/profile/dao/colleague_init.xml"})
class ColleagueDAOTest extends AbstractDAOTest {

    @Autowired
    private ColleagueDAO dao;

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";
    private static final UUID COLLEAGUE_UUID_1 = UUID.fromString("119e0d2b-1dc2-409f-8198-ecd66e59d47a");
    private static final UUID MANAGER_UUID_1 = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void updateSucceeded() {
        var colleague = getCorrectColleague();

        final int updated = dao.update(colleague);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void updateThrowDataIntegrityViolationException() {
        var colleague = getIncorrectColleague();

        assertThatCode(() -> dao.update(colleague))
                .isExactlyInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ERROR: insert or update on table \"colleague\" violates foreign key constraint");
    }

    @Test
    void insertJobWithExistsId() {
        var job = getJob("1");
        final int inserted = dao.insertJob(job);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertJobWithNotExistsId() {
        var job = getJob("3");
        final int inserted = dao.insertJob(job);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertCountryWithExistsCode() {
        var country = getCountry("GB");
        final int inserted = dao.insertCountry(country);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertCountryWithNotExistsCode() {
        var country = getCountry("ZZ");
        final int inserted = dao.insertCountry(country);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertWorkLevelWithExistsCode() {
        var workLevel = getWorkLevel("WL1");
        final int inserted = dao.insertWorkLevel(workLevel);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertWorkLevelWithNotExistsCode() {
        var workLevel = getWorkLevel("WL7");
        final int inserted = dao.insertWorkLevel(workLevel);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertDepartmentWithExistsId() {
        var department = getDepartment("1");
        final int inserted = dao.insertDepartment(department);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void insertDepartmentWithNotExistsId() {
        var department = getDepartment("5");
        final int inserted = dao.insertDepartment(department);
        assertThat(inserted).isEqualTo(1);
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