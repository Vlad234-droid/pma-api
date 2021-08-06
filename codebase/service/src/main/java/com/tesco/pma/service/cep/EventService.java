package com.tesco.pma.service.cep;

import com.tesco.pma.api.Identified;

public interface EventService {

    /**
     * Sends event to CEP
     *
     * @param payload - event payload
     * @param <T>     - event payload type
     */
    <T> void sendEvent(Identified<T> payload);
}
