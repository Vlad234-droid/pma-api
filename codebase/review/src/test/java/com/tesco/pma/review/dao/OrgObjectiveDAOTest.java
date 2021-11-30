package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.OrgObjective;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class OrgObjectiveDAOTest extends AbstractDAOTest {

    private static final UUID ORG_OBJECTIVE_UUID = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID ORG_OBJECTIVE_UUID_2 = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID ORG_OBJECTIVE_UUID_NOT_EXIST = UUID.fromString("aab9ab0b-f50f-4442-8900-000000000000");
    private static final Integer NUMBER_1 = 1;
    private static final String TITLE_1 = "Title #1";
    private static final String ORG_TITLE_UPDATE = "Title #1 updated";
    private static final Integer VERSION_1 = 1;
    private static final Integer VERSION_3 = 3;

    @Autowired
    private OrgObjectiveDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DataSet("cleanup.xml")
    @ExpectedDataSet("pm_org_objective_create_expected_1.xml")
    void createOrgObjectiveSucceeded() {

        final var orgObjective = OrgObjective.builder()
                .uuid(ORG_OBJECTIVE_UUID)
                .number(NUMBER_1)
                .status(OrgObjectiveStatus.DRAFT)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        final int rowsInserted = instance.create(orgObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void createOrgObjectiveAlreadyExist() {

        final var orgObjective = OrgObjective.builder()
                .uuid(ORG_OBJECTIVE_UUID_2)
                .number(NUMBER_1)
                .status(OrgObjectiveStatus.DRAFT)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        Assertions.assertThatThrownBy(() -> instance.create(orgObjective))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void getOrgObjective() {
        final var result = instance.read(ORG_OBJECTIVE_UUID_2);

        assertThat(result)
                .asInstanceOf(type(OrgObjective.class))
                .returns(NUMBER_1, from(OrgObjective::getNumber))
                .returns(OrgObjectiveStatus.DRAFT, from(OrgObjective::getStatus))
                .returns(VERSION_1, from(OrgObjective::getVersion));
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void getOrgObjectiveNotExist() {
        final var result = instance.read(ORG_OBJECTIVE_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void getOrgObjectives() {
        final var result = instance.getAll();

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.get(0))
                .returns(NUMBER_1, from(OrgObjective::getNumber))
                .returns(OrgObjectiveStatus.DRAFT, from(OrgObjective::getStatus))
                .returns(ORG_TITLE_UPDATE, from(OrgObjective::getTitle))
                .returns(VERSION_3, from(OrgObjective::getVersion));
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void deleteOrgObjectiveNotExist() {
        final var result = instance.delete(ORG_OBJECTIVE_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void deleteOrgObjectiveSucceeded() {
        final var result = instance.delete(ORG_OBJECTIVE_UUID_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    void getMaxVersionOrgObjective() {
        final var result = instance.getMaxVersion();
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    @ExpectedDataSet("pm_org_objective_publish_expected_1.xml")
    void publishOrgObjectiveSucceeded() {
        final var result = instance.publish();
        assertThat(result).isOne();
    }

    @Test
    @DataSet("pm_org_objective_init.xml")
    @ExpectedDataSet("pm_org_objective_unpublish_expected_1.xml")
    void unpublishOrgObjectiveSucceeded() {
        final var result = instance.unpublish();
        assertThat(result).isOne();
    }

}
