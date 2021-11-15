package com.tesco.pma.user;

import com.tesco.pma.api.User;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.NotNull;
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
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByColleagueUuid(@NotNull UUID colleagueUuid);

    /**
     * Finds user by iam id.
     *
     * @param iamId    iam id, not null.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByIamId(@NotNull String iamId);

    /**
     * Finds user by {@link Authentication}.
     *
     * @param authentication authentication, not null.
     * @return Optional with user, {@link Optional#empty()} if not found.
     */
    Optional<User> findUserByAuthentication(@NotNull Authentication authentication);
}
