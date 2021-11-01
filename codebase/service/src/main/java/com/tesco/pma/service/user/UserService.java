package com.tesco.pma.service.user;

import com.tesco.pma.api.User;
import com.tesco.pma.validation.UserRolesAllowed;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * User service.
 */
public interface UserService {
    /**
     * Finds user by colleague uuid.
     *
     * @param colleagueUuid colleague uuid, not null.
     * @param includes      additional data to be included.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByColleagueUuid(@NotNull UUID colleagueUuid, Collection<UserIncludes> includes);

    /**
     * Finds user by iam id.
     *
     * @param iamId    iam id, not null.
     * @param includes additional data to be included.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByIamId(@NotNull String iamId, Collection<UserIncludes> includes);

    /**
     * Finds users that have subsidiary permission assigned.
     *
     * @param subsidiaryUuid subsidiary uuid, not null.
     * @param role           role, could be null - means all roles.
     * @param includes       additional data to be included.
     * @return found users, not null.
     * @see com.tesco.pma.api.security.SubsidiaryPermission
     */
    Collection<User> findUsersHasSubsidiaryPermission(@NotNull UUID subsidiaryUuid,
                                                      @UserRolesAllowed String role,
                                                      Collection<UserIncludes> includes);

    /**
     * Finds user by {@link Authentication}.
     *
     * @param authentication authentication, not null.
     * @param includes       additional data to be included.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByAuthentication(@NotNull Authentication authentication, Collection<UserIncludes> includes);

    /**
     * Takes {@link Authentication} from the current security context and finds user by it.
     *
     * @param includes       additional data to be included.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> currentUser(Collection<UserIncludes> includes);
}
