package com.tesco.pma.configuration.security;

import com.tesco.pma.colleague.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmaMethodSecurityExpressionOperationsTest {

    private PmaMethodSecurityExpressionOperations instance;

    @Mock
    private MethodSecurityExpressionOperations mockMethodSecurityExpressionOperations;

    @Mock
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        instance = new PmaMethodSecurityExpressionOperations(mockMethodSecurityExpressionOperations, profileService);
    }

    @Test
    void isAdmin() {
        when(mockMethodSecurityExpressionOperations.hasRole("Admin")).thenReturn(true);

        assertThat(instance.isAdmin()).isTrue();
    }

    @Test
    void isColleague() {
        when(mockMethodSecurityExpressionOperations.hasRole("Colleague")).thenReturn(true);

        assertThat(instance.isColleague()).isTrue();
    }

    @Test
    void isLineManager() {
        when(mockMethodSecurityExpressionOperations.hasRole("LineManager")).thenReturn(true);

        assertThat(instance.isLineManager()).isTrue();
    }

    @Test
    void isPeopleTeam() {
        when(mockMethodSecurityExpressionOperations.hasRole("PeopleTeam")).thenReturn(true);

        assertThat(instance.isPeopleTeam()).isTrue();
    }

    @Test
    void isTalentAdmin() {
        when(mockMethodSecurityExpressionOperations.hasRole("TalentAdmin")).thenReturn(true);

        assertThat(instance.isTalentAdmin()).isTrue();
    }

    @Test
    void isProcessManager() {
        when(mockMethodSecurityExpressionOperations.hasRole("ProcessManager")).thenReturn(true);

        assertThat(instance.isProcessManager()).isTrue();
    }

}