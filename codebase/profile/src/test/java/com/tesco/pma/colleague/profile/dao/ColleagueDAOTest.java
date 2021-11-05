package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.organisation.api.Colleague;
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

    private Colleague getCorrectColleague() {
        var colleague = new Colleague();
        colleague.setUuid(COLLEAGUE_UUID_1);
        colleague.setManager(false);
        colleague.setManagerUuid(MANAGER_UUID_1);

        colleague.setJob(getJob("1"));
        colleague.setCountry(getCountry("GB"));
        colleague.setWorkLevel(getWorkLevel("WL1"));
        colleague.setDepartment(getDepartment("1"));

        return colleague;
    }

    private Colleague getIncorrectColleague() {
        var colleague = new Colleague();
        colleague.setUuid(COLLEAGUE_UUID_1);
        colleague.setManager(false);
        colleague.setManagerUuid(MANAGER_UUID_1);

        colleague.setJob(getJob("3"));
        colleague.setCountry(getCountry("DE"));
        colleague.setWorkLevel(getWorkLevel("WL7"));
        colleague.setDepartment(getDepartment("5"));

        return colleague;
    }


    private Colleague.Job getJob(String id) {
        Colleague.Job job = new Colleague.Job();
        job.setId(id);
        return job;
    }

    private Colleague.Country getCountry(String code) {
        Colleague.Country country = new Colleague.Country();
        country.setCode(code);
        return country;
    }

    private Colleague.WorkLevel getWorkLevel(String code) {
        Colleague.WorkLevel workLevel = new Colleague.WorkLevel();
        workLevel.setCode(code);
        return workLevel;
    }

    private Colleague.Department getDepartment(String id) {
        Colleague.Department department = new Colleague.Department();
        department.setId(id);
        return department;
    }

}