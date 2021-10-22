package com.tesco.pma.api;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 19:05
 */
public interface StatusAware<T> {
    T getStatus();
}
