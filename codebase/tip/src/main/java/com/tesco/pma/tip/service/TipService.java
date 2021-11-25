package com.tesco.pma.tip.service;

import com.tesco.pma.tip.api.Tip;

import java.util.List;
import java.util.UUID;

/**
 * Service Interface for managing {@link Tip}.
 */
public interface TipService {

    /**
     * @param tip the entity to create
     * @return tip with new uuid
     */
    Tip create(Tip tip);

    /**
     * @return the list of tips
     */
    List<Tip> findAll();

    /**
     * @param uuid of the entity
     * @return the tip
     */
    Tip findOne(UUID uuid);

    /**
     * @param tip the entity to update
     * @return tip
     */
    Tip update(Tip tip);

    /**
     * @param uuid of the entity
     */
    void delete(UUID uuid);

    /**
     * @param uuid of the entity
     */
    void publish(UUID uuid);
}
