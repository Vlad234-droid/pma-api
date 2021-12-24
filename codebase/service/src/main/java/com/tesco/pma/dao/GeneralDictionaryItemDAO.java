package com.tesco.pma.dao;

import com.tesco.pma.api.GeneralDictionaryItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GeneralDictionaryItemDAO {
    /**
     * @param dictionary table name
     * @param id identifier
     * @return dictionary item
     */
    GeneralDictionaryItem read(@Param("dictionary") String dictionary, Integer id);

    /**
     * @param dictionary table name
     * @param code dictionary code
     * @return dictionary item
     */
    GeneralDictionaryItem findByCode(@Param("dictionary") String dictionary, String code);

    /**
     * @param dictionary table name
     * @return list of dictionary items
     */
    List<GeneralDictionaryItem> findAll(@Param("dictionary") String dictionary);
}
