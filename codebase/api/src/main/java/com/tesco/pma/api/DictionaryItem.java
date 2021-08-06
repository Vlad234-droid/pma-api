package com.tesco.pma.api;

/**
 * Identified dictionary element
 */
public interface DictionaryItem extends Identified<Integer> {

    /**
     * Read identifier of dictionary element
     * @return  identifier of the element
     */
    @Override
    Integer getId();

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
