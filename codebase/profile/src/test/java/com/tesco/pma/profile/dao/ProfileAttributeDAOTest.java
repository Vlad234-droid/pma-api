package com.tesco.pma.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileAttributeDAOTest extends AbstractDAOTest {

    private static final String COLLEAGUE_UUID_1_STRING = "6d37262f-3a00-4706-a74b-6bf98be65765";
    private static final UUID COLLEAGUE_UUID_1 = UUID.fromString(COLLEAGUE_UUID_1_STRING);

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Autowired
    private ProfileAttributeDAO instance;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DataSet({"profile_attributes_init.xml"})
    void get() {
        final var result = instance.get(COLLEAGUE_UUID_1);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
    }

}