package com.tesco.pma.configuration.security;

import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.security.UserRoleNames.SUBSIDIARY_MANAGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmaMethodSecurityExpressionOperationsTest {

    private PmaMethodSecurityExpressionOperations instance;

    @Mock
    private MethodSecurityExpressionOperations mockMethodSecurityExpressionOperations;

    @Mock
    private UserService mockUserService;

    @BeforeEach
    void setUp() {
        instance = new PmaMethodSecurityExpressionOperations(mockMethodSecurityExpressionOperations, mockUserService);
    }

    @Test
    void isAdmin() {
        when(mockMethodSecurityExpressionOperations.hasRole("Admin")).thenReturn(true);

        assertThat(instance.isAdmin()).isTrue();
        noUserLookup();
    }

    @Test
    void isViewer() {
        when(mockMethodSecurityExpressionOperations.hasRole("Viewer")).thenReturn(true);

        assertThat(instance.isViewer()).isTrue();
        noUserLookup();
    }

    @Test
    void isManagerOfWhenNoUserFoundReturnsFalse() {
        when(mockMethodSecurityExpressionOperations.hasRole(SUBSIDIARY_MANAGER)).thenReturn(true);
        when(mockUserService.findUserByAuthentication(any(), eq(Set.of(UserIncludes.SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.empty());

        assertThat(instance.isManagerOf(UUID.randomUUID())).isFalse();
    }


    @Test
    void isManagerOfWhenNotInCorrectRoleReturnsFalse() {
        when(mockMethodSecurityExpressionOperations.hasRole(SUBSIDIARY_MANAGER)).thenReturn(false);

        assertThat(instance.isManagerOf(UUID.randomUUID())).isFalse();
        noUserLookup();
    }

    @Test
    void isManagerOfWhenNotManagerForSubsidiaryReturnsFalse() {
        final var subsidiaryUuid = UUID.randomUUID();
        when(mockMethodSecurityExpressionOperations.hasRole(SUBSIDIARY_MANAGER)).thenReturn(true);
        when(mockUserService.findUserByAuthentication(any(), eq(Set.of(UserIncludes.SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.of(new User(UUID.randomUUID())));

        assertThat(instance.isManagerOf(subsidiaryUuid)).isFalse();
    }

    @Test
    void isManagerOfSucceeded() {
        final var subsidiaryUuid = UUID.randomUUID();
        final var colleagueUuid = UUID.randomUUID();
        when(mockMethodSecurityExpressionOperations.hasRole(SUBSIDIARY_MANAGER)).thenReturn(true);
        final var user = new User(colleagueUuid);
        user.getSubsidiaryPermissions().add(SubsidiaryPermission.of(colleagueUuid, subsidiaryUuid, SUBSIDIARY_MANAGER));
        when(mockUserService.findUserByAuthentication(any(), eq(Set.of(UserIncludes.SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.of(user));

        assertThat(instance.isManagerOf(subsidiaryUuid)).isTrue();
    }

    private void noUserLookup() {
        verifyNoInteractions(mockUserService);
    }
}