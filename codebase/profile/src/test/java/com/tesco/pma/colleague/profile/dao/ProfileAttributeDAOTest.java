package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.colleague.profile.domain.AttributeType;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProfileAttributeDAOTest extends AbstractDAOTest {

    private static final String COLLEAGUE_UUID_1_STRING = "6d37262f-3a00-4706-a74b-6bf98be65765";
    private static final UUID COLLEAGUE_UUID_1 = UUID.fromString(COLLEAGUE_UUID_1_STRING);

    private static final String[][] TEST_DATA = {
            {"emergencyContact", "Emergency contact", "Full name", "STRING"},
            {"emergencyPhone", "Emergency phone", "+44 123 456789", "STRING"},
            {"businessUnitBonus", "Business Unit Bonus", "Value", "STRING"}
    };

    @Autowired
    private ProfileAttributeDAO instance;

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";

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
    @DataSet({BASE_PATH_TO_DATA_SET + "profile_attributes_init.xml"})
    void get() {
        final var result = instance.get(COLLEAGUE_UUID_1);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "cleanup.xml"})
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "profile_attributes_insert_expected_1.xml")
    void insertSucceeded() {
        List<TypedAttribute> profileAttributes = profileAttributes(3);
        profileAttributes.forEach(profileAttribute -> {
            final var result = instance.create(profileAttribute);

            assertThat(result).isOne();

        });
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "profile_attributes_init.xml"})
    void insertAlreadyExistsWithSameName() {
        TypedAttribute profileAttribute = profileAttribute(1);

        assertThatCode(() -> instance.create(profileAttribute))
                .isExactlyInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining(COLLEAGUE_UUID_1.toString());
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "profile_attributes_init.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "profile_attributes_update_expected_1.xml")
    void updateSucceeded() {
        List<TypedAttribute> profileAttributes = profileAttributes(3);
        profileAttributes.forEach(profileAttribute -> {
            String updatedValue = profileAttribute.getValue() + "(Updated)";
            profileAttribute.setValue(updatedValue);
        });

        profileAttributes.forEach(profileAttribute -> {
            final var result = instance.update(profileAttribute);

            assertThat(result).isOne();

        });
    }

    @Test
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "profile_attributes_delete_expected_1.xml")
    void deleteSucceeded() {
        List<TypedAttribute> profileAttributes = profileAttributes(3);
        profileAttributes.forEach(profileAttribute -> {
            final var result = instance.delete(profileAttribute);

            assertThat(result).isOne();

        });
    }


    private List<TypedAttribute> profileAttributes(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(this::profileAttribute)
                .collect(Collectors.toList());
    }

    private TypedAttribute profileAttribute(int index) {
        TypedAttribute profileAttribute = new TypedAttribute();
        profileAttribute.setColleagueUuid(COLLEAGUE_UUID_1);
        profileAttribute.setName(TEST_DATA[index - 1][0]);
        profileAttribute.setValue(TEST_DATA[index - 1][2]);
        profileAttribute.setType(AttributeType.valueOf(TEST_DATA[index - 1][3]));
        return profileAttribute;
    }

}