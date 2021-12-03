package com.tesco.pma.tip.service;

import com.tesco.pma.pagination.RequestQuery;
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
     * @param requestQuery filter, sort, offset
     * @return the list of tips
     */
    List<Tip> findAll(RequestQuery requestQuery);

    /**
     * @param uuid of the entity
     * @return the tip
     */
    Tip findOne(UUID uuid);

    /**
     * @param uuid of the entity
     * @return history
     */
    List<Tip> findHistory(UUID uuid);

    /**
     * @param uuid of the entity
     * @param withHistory delete all versions
     */
    void delete(UUID uuid, boolean withHistory);

    /**
     * @param uuid of the entity
     * @return the tip
     */
    Tip publish(UUID uuid);

}
