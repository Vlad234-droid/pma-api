package com.tesco.pma.api;


/**
 * Generic Identified Element
 */
public interface Identified<T> {
    /**
     * Read Identification value of Element
     * @return identifier of the element represented by generic type
     */
    T getId();
}