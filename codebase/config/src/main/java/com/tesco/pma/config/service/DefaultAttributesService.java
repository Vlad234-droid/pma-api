package com.tesco.pma.config.service;

import java.util.UUID;

/**
 * Default attributes service
 */
public interface DefaultAttributesService {

    /**
     * Update profile attributes for colleague by default attributes
     *
     * @param colleagueId colleague uuid, not null.
     */
    void updateDefaultAttributes(UUID colleagueId);

}
