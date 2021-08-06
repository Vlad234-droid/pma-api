package com.tesco.pma.service.security;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.SubsidiaryPermissionDAO;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_NOT_FOUND;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_PERMISSION_ALREADY_EXISTS;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_PERMISSION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubsidiaryPermissionServiceImplTest {
    static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    static final EasyRandom RANDOM = new EasyRandom();

    private SubsidiaryPermissionServiceImpl instance;

    @Mock
    private SubsidiaryPermissionDAO mockSubsidiaryPermissionDAO;
    @Mock
    private NamedMessageSourceAccessor mockNamedMessageSourceAccessor;


    @BeforeEach
    void setUp() {
        instance = new SubsidiaryPermissionServiceImpl(mockSubsidiaryPermissionDAO, mockNamedMessageSourceAccessor);
    }

    @Test
    void grantSubsidiaryPermissionSucceeded() {
        final var subsidiaryPermission = RANDOM.nextObject(SubsidiaryPermission.class);
        when(mockSubsidiaryPermissionDAO.create(subsidiaryPermission)).thenReturn(1);

        assertDoesNotThrow(() -> instance.grantSubsidiaryPermission(subsidiaryPermission));
    }

    @Test
    void grantSubsidiaryPermissionSubsidiaryNotFound() {
        final var subsidiaryPermission = RANDOM.nextObject(SubsidiaryPermission.class);
        when(mockSubsidiaryPermissionDAO.create(subsidiaryPermission)).thenReturn(0);
        when(mockNamedMessageSourceAccessor
                .getMessage(SUBSIDIARY_NOT_FOUND, Map.of("subsidiaryUuid", subsidiaryPermission.getSubsidiaryUuid())))
                .thenReturn(ERROR_MESSAGE);

        assertThatCode(() -> instance.grantSubsidiaryPermission(subsidiaryPermission))
                .hasMessage(ERROR_MESSAGE)
                .asInstanceOf(type(NotFoundException.class))
                .returns(SUBSIDIARY_NOT_FOUND.getCode(), from(AbstractApiRuntimeException::getCode));
    }

    @Test
    void grantSubsidiaryPermissionAlreadyExists() {
        final var subsidiaryPermission = RANDOM.nextObject(SubsidiaryPermission.class);
        final var duplicateKeyException = RANDOM.nextObject(DuplicateKeyException.class);

        when(mockSubsidiaryPermissionDAO.create(subsidiaryPermission)).thenThrow(duplicateKeyException);
        when(mockNamedMessageSourceAccessor.getMessage(SUBSIDIARY_PERMISSION_ALREADY_EXISTS,
                Map.of("subsidiaryUuid", subsidiaryPermission.getSubsidiaryUuid(),
                        "colleagueUuid", subsidiaryPermission.getColleagueUuid(),
                        "role", subsidiaryPermission.getRole())))
                .thenReturn(ERROR_MESSAGE);

        assertThatCode(() -> instance.grantSubsidiaryPermission(subsidiaryPermission))
                .hasMessage(ERROR_MESSAGE)
                .asInstanceOf(type(AlreadyExistsException.class))
                .returns(SUBSIDIARY_PERMISSION_ALREADY_EXISTS.getCode(), from(AbstractApiRuntimeException::getCode));
    }

    @Test
    void revokeSubsidiaryPermissionSucceeded() {
        final var subsidiaryPermission = RANDOM.nextObject(SubsidiaryPermission.class);
        when(mockSubsidiaryPermissionDAO.delete(subsidiaryPermission)).thenReturn(1);

        assertDoesNotThrow(() -> instance.revokeSubsidiaryPermission(subsidiaryPermission));
    }

    @Test
    void revokeSubsidiaryPermissionPermissionNotFound() {
        final var subsidiaryPermission = RANDOM.nextObject(SubsidiaryPermission.class);
        when(mockSubsidiaryPermissionDAO.delete(subsidiaryPermission)).thenReturn(0);
        when(mockNamedMessageSourceAccessor.getMessage(SUBSIDIARY_PERMISSION_NOT_FOUND,
                Map.of("subsidiaryUuid", subsidiaryPermission.getSubsidiaryUuid(),
                        "colleagueUuid", subsidiaryPermission.getColleagueUuid(),
                        "role", subsidiaryPermission.getRole())))
                .thenReturn(ERROR_MESSAGE);

        assertThatCode(() -> instance.revokeSubsidiaryPermission(subsidiaryPermission))
                .hasMessage(ERROR_MESSAGE)
                .asInstanceOf(type(NotFoundException.class))
                .returns(SUBSIDIARY_PERMISSION_NOT_FOUND.getCode(), from(AbstractApiRuntimeException::getCode));
    }

    @Test
    void findSubsidiaryPermissionsForSubsidiary() {
        final var subsidiaryUuid = RANDOM.nextObject(UUID.class);
        Set<SubsidiaryPermission> subsidiaryPermissions = RANDOM.objects(SubsidiaryPermission.class, 5).collect(Collectors.toSet());
        when(mockSubsidiaryPermissionDAO.findBySubsidiaryUuid(subsidiaryUuid)).thenReturn(subsidiaryPermissions);

        final var res = instance.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid);

        assertThat(res).containsExactlyElementsOf(subsidiaryPermissions);
    }

    @Test
    void findSubsidiaryPermissionsForUser() {
        final var colleagueUuid = UUID.randomUUID();
        Set<SubsidiaryPermission> subsidiaryPermissions = RANDOM.objects(SubsidiaryPermission.class, 5).collect(Collectors.toSet());
        when(mockSubsidiaryPermissionDAO.findByColleagueUuid(colleagueUuid)).thenReturn(subsidiaryPermissions);

        final var res = instance.findSubsidiaryPermissionsForUser(colleagueUuid);

        assertThat(res).containsExactlyElementsOf(subsidiaryPermissions);
    }

    @Test
    void findSubsidiaryPermissionsForUsers() {
        final var colleagueUuid1 = UUID.randomUUID();
        final var colleagueUuid2 = UUID.randomUUID();
        final var colleagueUuidEmpty = UUID.randomUUID();
        Set<SubsidiaryPermission> subsidiaryPermissions1 = RANDOM.objects(SubsidiaryPermission.class, 5)
                .peek(subsidiaryPermission -> subsidiaryPermission.setColleagueUuid(colleagueUuid1))
                .collect(Collectors.toSet());
        Set<SubsidiaryPermission> subsidiaryPermissions2 = RANDOM.objects(SubsidiaryPermission.class, 5)
                .peek(subsidiaryPermission -> subsidiaryPermission.setColleagueUuid(colleagueUuid2))
                .collect(Collectors.toSet());
        Set<SubsidiaryPermission> all = new HashSet<>(subsidiaryPermissions1);
        all.addAll(subsidiaryPermissions2);
        when(mockSubsidiaryPermissionDAO.findByColleagueUuids(eq(List.of(colleagueUuid1, colleagueUuid2, colleagueUuidEmpty))))
                .thenReturn(all);

        final var res = instance.findSubsidiaryPermissionsForUsers(
                List.of(colleagueUuid1, colleagueUuid2, colleagueUuidEmpty));

        assertThat(res)
                .containsOnlyKeys(colleagueUuid1, colleagueUuid2)
                .hasEntrySatisfying(colleagueUuid1, subsidiaryPermissions -> assertThat(subsidiaryPermissions).containsExactlyElementsOf(subsidiaryPermissions1))
                .hasEntrySatisfying(colleagueUuid2, subsidiaryPermissions -> assertThat(subsidiaryPermissions).containsExactlyElementsOf(subsidiaryPermissions2));
    }
}