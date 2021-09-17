package com.tesco.pma.objective.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.ObjectiveStatus;
import com.tesco.pma.objective.domain.PersonalObjective;
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

class ObjectiveDAOTest extends AbstractDAOTest {

    private static final UUID GROUP_OBJECTIVE_UUID = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID GROUP_OBJECTIVE_UUID_2 = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID GROUP_OBJECTIVE_UUID_NOT_EXIST = UUID.fromString("aab9ab0b-f50f-4442-8900-000000000000");
    private static final UUID PERSONAL_OBJECTIVE_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID PERSONAL_OBJECTIVE_UUID_NOT_EXIST = UUID.fromString("ddb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID BUSINESS_UNIT_UUID = UUID.fromString("ffb9ab0b-f50f-4442-8900-b03777ee00ef");
    private static final UUID BUSINESS_UNIT_UUID_2 = UUID.fromString("ffb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ccb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final UUID PERFORMANCE_CYCLE_UUID_2 = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc942");
    private static final Integer SEQUENCE_NUMBER = 1;
    private static final String TITLE_1 = "Title #1";
    private static final String TITLE_UPDATE = "Title update";
    private static final String TITLE_INIT = "Title init";
    private static final String DESCRIPTION_INIT = "Description init";
    private static final String DESCRIPTION_UPDATE = "Description update";
    private static final String MEETS_INIT = "Meets init";
    private static final String MEETS_UPDATE = "Meets update";
    private static final String EXCEEDS_INIT = "Exceeds init";
    private static final String EXCEEDS_UPDATE = "Exceeds update";
    private static final Integer VERSION_1 = 1;
    private static final Integer VERSION_2 = 2;

    @Autowired
    private ObjectiveDAO instance;

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
    @ExpectedDataSet("group_objective_create_expected_1.xml")
    void createGroupObjectiveSucceeded() {

        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID)
                .businessUnitUuid(BUSINESS_UNIT_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_1)
                .version(VERSION_1)
                .status(ObjectiveStatus.DRAFT)
                .build();

        final int rowsInserted = instance.createGroupObjective(groupObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void createGroupObjectiveAlreadyExist() {

        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID_2)
                .businessUnitUuid(BUSINESS_UNIT_UUID_2)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID_2)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_1)
                .version(VERSION_1)
                .status(ObjectiveStatus.DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createGroupObjective(groupObjective))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getGroupObjective() {
        final var result = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_2);

        assertThat(result)
                .asInstanceOf(type(GroupObjective.class))
                .returns(BUSINESS_UNIT_UUID_2, from(GroupObjective::getBusinessUnitUuid))
                .returns(PERFORMANCE_CYCLE_UUID_2, from(GroupObjective::getPerformanceCycleUuid))
                .returns(SEQUENCE_NUMBER, from(GroupObjective::getSequenceNumber))
                .returns(VERSION_1, from(GroupObjective::getVersion))
                .returns(ObjectiveStatus.DRAFT, from(GroupObjective::getStatus));
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getGroupObjectiveNotExist() {
        final var result = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void deleteGroupObjectiveNotExist() {
        final var result = instance.deleteGroupObjective(GROUP_OBJECTIVE_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void deleteGroupObjectiveSucceeded() {
        final var result = instance.deleteGroupObjective(GROUP_OBJECTIVE_UUID_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void updateGroupObjectiveNotExist() {
        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID_NOT_EXIST)
                .businessUnitUuid(BUSINESS_UNIT_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_1)
                .version(VERSION_1)
                .status(ObjectiveStatus.DRAFT)
                .build();

        final var result = instance.updateGroupObjective(groupObjective);

        assertThat(result).isZero();
    }

    @Test
    @DataSet("group_objective_init.xml")
    @ExpectedDataSet("group_objective_update_expected_1.xml")
    void updateGroupObjectiveSucceeded() {
        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID_2)
                .businessUnitUuid(BUSINESS_UNIT_UUID_2)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID_2)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_UPDATE)
                .version(VERSION_2)
                .status(ObjectiveStatus.SUBMITTED)
                .build();

        final var result = instance.updateGroupObjective(groupObjective);

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "personal_objective_init.xml"})
    void getPersonalObjective() {
        final var groupObjective = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_2);
        final var result = instance.getPersonalObjective(PERSONAL_OBJECTIVE_UUID);

        assertThat(result)
                .asInstanceOf(type(PersonalObjective.class))
                .returns(COLLEAGUE_UUID, from(PersonalObjective::getColleagueUuid))
                .returns(PERFORMANCE_CYCLE_UUID, from(PersonalObjective::getPerformanceCycleUuid))
                .returns(SEQUENCE_NUMBER, from(PersonalObjective::getSequenceNumber))
                .returns(TITLE_INIT, from(PersonalObjective::getTitle))
                .returns(DESCRIPTION_INIT, from(PersonalObjective::getDescription))
                .returns(MEETS_INIT, from(PersonalObjective::getMeets))
                .returns(EXCEEDS_INIT, from(PersonalObjective::getExceeds))
                .returns(groupObjective, from(PersonalObjective::getGroupObjective))
                .returns(ObjectiveStatus.DRAFT, from(PersonalObjective::getStatus));
    }

    @Test
    @DataSet("personal_objective_init.xml")
    void getPersonalObjectiveNotExist() {
        final var result = instance.getPersonalObjective(PERSONAL_OBJECTIVE_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet("group_objective_init.xml")
    @ExpectedDataSet("personal_objective_create_expected_1.xml")
    void createPersonalObjectiveSucceeded() {
        final var groupObjective = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_2);
        final var personalObjective = PersonalObjective.builder()
                .uuid(PERSONAL_OBJECTIVE_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_INIT)
                .description(DESCRIPTION_INIT)
                .meets(MEETS_INIT)
                .exceeds(EXCEEDS_INIT)
                .groupObjective(groupObjective)
                .status(ObjectiveStatus.DRAFT)
                .build();

        final int rowsInserted = instance.createPersonalObjective(personalObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "personal_objective_init.xml"})
    void createPersonalObjectiveAlreadyExist() {

        final var personalObjective = PersonalObjective.builder()
                .uuid(PERSONAL_OBJECTIVE_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_INIT)
                .description(DESCRIPTION_INIT)
                .meets(MEETS_INIT)
                .exceeds(EXCEEDS_INIT)
                .status(ObjectiveStatus.DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createPersonalObjective(personalObjective))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"group_objective_init.xml", "personal_objective_init.xml"})
    void deletePersonalObjectiveNotExist() {
        final var result = instance.deletePersonalObjective(PERSONAL_OBJECTIVE_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("personal_objective_init.xml")
    void deletePersonalObjectiveSucceeded() {
        final var result = instance.deletePersonalObjective(PERSONAL_OBJECTIVE_UUID);
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "personal_objective_init.xml"})
    @ExpectedDataSet("personal_objective_update_expected_1.xml")
    void updatePersonalObjectiveSucceeded() {
        final var groupObjective = instance.getGroupObjective(GROUP_OBJECTIVE_UUID);
        final var personalObjective = PersonalObjective.builder()
                .uuid(PERSONAL_OBJECTIVE_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .sequenceNumber(SEQUENCE_NUMBER)
                .title(TITLE_UPDATE)
                .description(DESCRIPTION_UPDATE)
                .meets(MEETS_UPDATE)
                .exceeds(EXCEEDS_UPDATE)
                .groupObjective(groupObjective)
                .status(ObjectiveStatus.SUBMITTED)
                .build();

        final var result = instance.updatePersonalObjective(personalObjective);

        assertThat(result).isOne();
    }
}
