package com.tesco.pma.tip.service;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;

import java.util.List;

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
     * @param key of the entity
     * @return the tip
     */
    Tip findOne(String key);

    /**
     * @param key of the entity
     * @return history
     */
    List<Tip> findHistory(String key);

    /**
     * @param tip the entity to update
     * @return tip
     */
    Tip update(Tip tip);

    /**
     * @param key of the entity
     */
    void delete(String key);

    /**
     * @param key of the entity
     */
    void publish(String key);
}
