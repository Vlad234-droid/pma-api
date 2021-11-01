package com.tesco.pma.colleague.security.service.rest;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserManagementEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
// TODO Implement all tests
class UserManagementEndpointTest extends AbstractEndpointTest {

    private static final EasyRandom RANDOM = new EasyRandom();

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private UserManagementService mockUserManagementService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAccountsShouldReturnAllAccounts() throws Exception {

        when(mockUserManagementService.getAccounts(anyInt()))
                .thenReturn(randomObjects(Account.class, 3));

        mvc.perform(get("/user-management/accounts")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void createAccount() {
    }

    @Test
    void changeAccountStatus() {
    }

    @Test
    void getRolesShouldReturnAllRoles() throws Exception {

        when(mockUserManagementService.getRoles())
                .thenReturn(randomObjects(Role.class, 3));

        mvc.perform(get("/user-management/roles")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void grantRole() {
    }

    @Test
    void revokeRole() {
    }

    private <T> List<T> randomObjects(Class<T> type, int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(value -> RANDOM.nextObject(type))
                .collect(Collectors.toList());
    }

}