package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

// TODO
class RoleManagementDAOTest extends AbstractDAOTest {

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
    void get() {
    }

}