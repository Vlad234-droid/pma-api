package com.tesco.pma.service.security;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.SubsidiaryPermissionDAO;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_NOT_FOUND;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_PERMISSION_ALREADY_EXISTS;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_PERMISSION_NOT_FOUND;

/**
 * Implementation of {@link SubsidiaryPermissionService}.
 */
@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class SubsidiaryPermissionServiceImpl implements SubsidiaryPermissionService {

    private static final String SUBSIDIARY_UUID = "subsidiaryUuid";
    private static final String COLLEAGUE_UUID = "colleagueUuid";
    private static final String ROLE = "role";

    private final SubsidiaryPermissionDAO subsidiaryPermissionDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public void grantSubsidiaryPermission(final SubsidiaryPermission permission) {
        try {
            if (0 == subsidiaryPermissionDAO.create(permission)) {
                throw new NotFoundException(SUBSIDIARY_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(SUBSIDIARY_NOT_FOUND, Map.of(SUBSIDIARY_UUID, permission.getSubsidiaryUuid())));
            }
        } catch (DuplicateKeyException e) {
            throw new AlreadyExistsException(SUBSIDIARY_PERMISSION_ALREADY_EXISTS.name(),
                    messageSourceAccessor.getMessage(SUBSIDIARY_PERMISSION_ALREADY_EXISTS,
                            Map.of(SUBSIDIARY_UUID, permission.getSubsidiaryUuid(),
                                    COLLEAGUE_UUID, permission.getColleagueUuid(),
                                    ROLE, permission.getRole())), e);
        }
    }

    @Override
    @Transactional
    public void revokeSubsidiaryPermission(final SubsidiaryPermission permission) {
        final var res = subsidiaryPermissionDAO.delete(permission);
        if (1 != res) {
            throw new NotFoundException(SUBSIDIARY_PERMISSION_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(SUBSIDIARY_PERMISSION_NOT_FOUND,
                            Map.of(SUBSIDIARY_UUID, permission.getSubsidiaryUuid(),
                                    COLLEAGUE_UUID, permission.getColleagueUuid(),
                                    ROLE, permission.getRole())));
        }
    }

    @Override
    public Collection<SubsidiaryPermission> findSubsidiaryPermissionsForSubsidiary(final UUID subsidiaryUuid) {
        return subsidiaryPermissionDAO.findBySubsidiaryUuid(subsidiaryUuid);
    }

    @Override
    public Collection<SubsidiaryPermission> findSubsidiaryPermissionsForUser(final UUID colleagueUuid) {
        return subsidiaryPermissionDAO.findByColleagueUuid(colleagueUuid);
    }

    @Override
    public Map<UUID, Collection<SubsidiaryPermission>> findSubsidiaryPermissionsForUsers(final Collection<UUID> colleagueUuids) {
        return subsidiaryPermissionDAO.findByColleagueUuids(colleagueUuids).stream()
                .collect(Collectors.groupingBy(SubsidiaryPermission::getColleagueUuid, Collectors.toCollection(ArrayList::new)));
    }
}