package com.tesco.pma.colleague.security.service.rest;

import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = UserManagementEndpoint.class)
// TODO
class UserManagementEndpointTest extends AbstractEndpointTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAccounts() {
    }

    @Test
    void createAccount() {
    }

    @Test
    void disableAccount() {
    }

    @Test
    void enableAccount() {
    }

    @Test
    void getRoles() {
    }

    @Test
    void grantRole() {
    }

    @Test
    void revokeRole() {
    }

}