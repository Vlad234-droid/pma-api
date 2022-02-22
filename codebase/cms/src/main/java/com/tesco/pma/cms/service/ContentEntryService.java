package com.tesco.pma.cms.service;

import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.pagination.RequestQuery;

import java.util.List;
import java.util.UUID;

/**
 * Content Entry Service
 * */
public interface ContentEntryService {

    /**
     * Create a new Content Entry
     *
     * @param contentEntry - content entry to be created.
     * @return create content entry.
     */
    ContentEntry create(ContentEntry contentEntry);

    /**
     * Find content entry by key
     *
     * @param key - content entry key.
     * @return list of ContentEntry.
     */
    List<ContentEntry> findByKey(String key);

    /**
     * Find content entry by RequestQuery
     *
     * @param requestQuery - request query.
     * @return list of ContentEntry.
     */
    List<ContentEntry> findByRequestQuery(RequestQuery requestQuery);

    /**
     * Find content entry by ID
     *
     * @param uuid - content entry ID.
     * @return ContentEntry instance.
     */
    ContentEntry findById(UUID uuid);

    /**
     * Delete a content entry
     *
     * @param deleteUuid - content entry ID.
     *
     */
    void delete(UUID deleteUuid);

    /**
     * Update a content entry
     *
     * @param contentEntry - content entry to update.
     * @return ContentEntry instance.
     */
    ContentEntry update(ContentEntry contentEntry);
}
