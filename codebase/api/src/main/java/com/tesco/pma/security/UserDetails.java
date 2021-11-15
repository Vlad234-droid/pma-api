package com.tesco.pma.security;

import com.tesco.pma.api.User;
import lombok.NonNull;
import lombok.experimental.Delegate;

/**
 * Wrapper for {@link User} used in authorization process.
 */
public class UserDetails extends User {

    @Delegate
    private final User user;

    public UserDetails(@NonNull User user) {
        this.user = user;
    }

}
