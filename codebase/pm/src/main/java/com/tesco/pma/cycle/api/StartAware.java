package com.tesco.pma.cycle.api;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-18 12:24
 */
public interface StartAware<T> {
    T getStart();

    String getISOStart();

    void setStart(T start);

    void setISOStart(String start);
}
