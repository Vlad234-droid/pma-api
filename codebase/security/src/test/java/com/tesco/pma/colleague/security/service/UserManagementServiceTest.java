package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.security.LocalTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
// TODO
class UserManagementServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getRoles() {
    }

    @Test
    void getAccounts() {
    }

    @Test
    void createAccount() {
    }

    @Test
    void grantRole() {
    }

    @Test
    void revokeRole() {
    }

    @Test
    void disableAccount() {
    }

    @Test
    void enableAccount() {
    }

}