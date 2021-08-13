package com.tesco.pma.api;

import java.io.Serializable;

/**
 * Identified dictionary element
 */
public interface DictionaryItem<T extends Serializable> extends Identified<T> {

    /**
     * Read code related to identifier of dictionary element
     * @return  code of the element
     */
    String getCode();

    /**
     * Read description of dictionary element
     * @return  description of the element
     */
    String getDescription();
}
