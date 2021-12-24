package com.tesco.pma.service;

import com.tesco.pma.api.GeneralDictionaryItem;

import java.util.List;

public interface GeneralDictionaryItemService {
    /**
     *
     * @param dictionary table name
     * @param id identifier
     * @return dictionary item
     */
    GeneralDictionaryItem read(String dictionary, Integer id);

    /**
     *
     * @param dictionary table name
     * @param code dictionary code
     * @return dictionary item
     */
    GeneralDictionaryItem findByCode(String dictionary, String code);

    /**
     * @param dictionary table name
     * @return list of dictionary items
     */
    List<GeneralDictionaryItem> findAll(String dictionary);

}
