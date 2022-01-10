package com.tesco.pma.colleague.security.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.colleague.security.configuration.UserManagementProperties;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.domain.request.RoleRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserManagementEndpoint.class)
// TODO Implement all tests
class UserManagementEndpointTest extends AbstractEndpointTest {

    private static final EasyRandom RANDOM = new EasyRandom();

    private JacksonTester<CreateAccountRequest> createAccountRequestJsonTester;
    private JacksonTester<ChangeAccountStatusRequest> changeAccountStatusRequestJsonTester;
    private JacksonTester<RoleRequest> roleRequestJsonTester;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private UserManagementService mockUserManagementService;

    @MockBean
    private UserManagementProperties mockUserManagementProperties;

    public static final String ACCOUNTS_URL_TEMPLATE = "/user-management/accounts";
    public static final String ROLES_URL_TEMPLATE = "/user-management/roles";

    private static final String TEST_USER_MANAGEMENT_SUBJECT = "test-user-management-subject";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAccountsWithAdminRoleShouldReturnAllAccounts() throws Exception {

        when(mockUserManagementService.getAccounts(anyInt()))
                .thenReturn(randomObjects(Account.class, 3));

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void getAccountsWithSubjectShouldReturnAllAccounts() throws Exception {

        when(mockUserManagementService.getAccounts(anyInt()))
                .thenReturn(randomObjects(Account.class, 3));
        when(mockUserManagementProperties.getSubject()).thenReturn(TEST_USER_MANAGEMENT_SUBJECT);

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(jwtWithSubject(TEST_USER_MANAGEMENT_SUBJECT))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void getAccountsUnauthorized() throws Exception {

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(APPLICATION_JSON));

        verifyNoInteractions(mockUserManagementProperties);

        verifyNoInteractions(mockUserManagementService);

    }

    @Test
    void getAccountsForbiddenWithSubjectNotMatch() throws Exception {

        when(mockUserManagementProperties.getSubject()).thenReturn(TEST_USER_MANAGEMENT_SUBJECT);

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(jwtWithSubject("not-user-management-subject"))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(mockUserManagementProperties, times(1))
                .getSubject();

        verifyNoInteractions(mockUserManagementService);

    }

    @Test
    void getAccountsWithNextPageTokenShouldReturnAllAccounts() throws Exception {

        when(mockUserManagementService.getAccounts(anyInt()))
                .thenReturn(randomObjects(Account.class, 3));

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .param("nextPageToken", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void createAccountWithOneRoleShouldReturnStatusCreated() throws Exception {

        // given
        CreateAccountRequest createAccountRequest = randomObject(CreateAccountRequest.class);
        createAccountRequest.setRoleId(1);

        // when
        ResultActions resultActions = mvc.perform(
                post(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(createAccountRequestJsonTester.write(createAccountRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void createAccountWithManyRolesShouldReturnStatusCreated() throws Exception {

        // given
        CreateAccountRequest createAccountRequest = randomObject(CreateAccountRequest.class);
        createAccountRequest.setRoleId(List.of(1, 2, 3));

        // when
        ResultActions resultActions = mvc.perform(
                post(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(createAccountRequestJsonTester.write(createAccountRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());
    }

    @Test
    void enableAccountShouldReturnStatusCreated() throws Exception {

        // given
        ChangeAccountStatusRequest changeAccountStatusRequest = randomObject(ChangeAccountStatusRequest.class);
        changeAccountStatusRequest.setStatus(AccountStatus.ENABLED);

        // when
        ResultActions resultActions = mvc.perform(
                put(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(changeAccountStatusRequestJsonTester.write(changeAccountStatusRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void disableAccountShouldReturnStatusCreated() throws Exception {

        // given
        ChangeAccountStatusRequest changeAccountStatusRequest = randomObject(ChangeAccountStatusRequest.class);
        changeAccountStatusRequest.setStatus(AccountStatus.DISABLED);

        // when
        ResultActions resultActions = mvc.perform(
                put(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(changeAccountStatusRequestJsonTester.write(changeAccountStatusRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }


    @Test
    void getRolesShouldReturnAllRoles() throws Exception {

        when(mockUserManagementService.getRoles())
                .thenReturn(randomObjects(Role.class, 3));

        mvc.perform(get(ACCOUNTS_URL_TEMPLATE)
                        .with(admin())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void grantRoleWithOneRoleShouldReturnStatusCreated() throws Exception {

        // given
        RoleRequest roleRequest = randomObject(RoleRequest.class);
        roleRequest.setRole(1);

        // when
        ResultActions resultActions = mvc.perform(
                post(ROLES_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(roleRequestJsonTester.write(roleRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void grantRoleWithManyRolesShouldReturnStatusCreated() throws Exception {

        // given
        RoleRequest roleRequest = randomObject(RoleRequest.class);
        roleRequest.setRole(List.of(1, 2, 3));

        // when
        ResultActions resultActions = mvc.perform(
                post(ROLES_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(roleRequestJsonTester.write(roleRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void revokeRoleWithOneRoleShouldReturnStatusCreated() throws Exception {

        // given
        RoleRequest roleRequest = randomObject(RoleRequest.class);
        roleRequest.setRole(1);

        // when
        ResultActions resultActions = mvc.perform(
                post(ROLES_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(roleRequestJsonTester.write(roleRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void revokeRoleWithManyRolesShouldReturnStatusCreated() throws Exception {

        // given
        RoleRequest roleRequest = randomObject(RoleRequest.class);
        roleRequest.setRole(List.of(1, 2, 3));

        // when
        ResultActions resultActions = mvc.perform(
                delete(ROLES_URL_TEMPLATE)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(roleRequestJsonTester.write(roleRequest).getJson()));

        // then
        andExpect(resultActions, status().isCreated());

    }


    private <T> T randomObject(Class<T> type) {
        return RANDOM.nextObject(type);
    }

    private <T> List<T> randomObjects(Class<T> type, int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(value -> RANDOM.nextObject(type))
                .collect(Collectors.toList());
    }

    private void andExpect(ResultActions resultActions,
                           ResultMatcher status) throws Exception {

        resultActions
                .andExpect(status)
                .andExpect(content().contentType(APPLICATION_JSON));

    }

}