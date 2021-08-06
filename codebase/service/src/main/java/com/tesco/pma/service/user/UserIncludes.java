package com.tesco.pma.service.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicates which additional part of User data should be included when fetching user.
 */
@Schema(enumAsRef = true)
public enum UserIncludes {
    SUBSIDIARY_PERMISSIONS;
}
