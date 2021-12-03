package com.tesco.pma.colleague.security.service;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.LocalTestConfig;
import com.tesco.pma.colleague.security.dao.AccountManagementDAO;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
// TODO Implement all tests
class UserManagementServiceTest {

    @MockBean
    private AccountManagementDAO mockAccountManagementDAO;

    @MockBean
    private ProfileService mockProfileService;

    @MockBean
    private NamedMessageSourceAccessor messages;

    @SpyBean
    private UserManagementServiceImpl userManagementService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getRoles() {
        when(mockAccountManagementDAO.findRoles())
                .thenReturn(roles(8));

        var roles = userManagementService.getRoles();
        assertThat(roles.size()).isEqualTo(8);
    }

    @Test
    void getAllRoles() {
        when(mockAccountManagementDAO.findAllRoles())
                .thenReturn(roles(12));

        var roles = userManagementService.getAllRoles();
        assertThat(roles.size()).isEqualTo(12);
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
    void changeAccountStatus() {
    }

    private List<Role> roles(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(this::role)
                .collect(Collectors.toList());
    }

    private Role role(int index) {
        Role role = new Role();
        role.setId(index);
        role.setCode("code" + index);
        role.setDescription("description" + index);
        return role;
    }

}