package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.DeliveryMode;

/**
 * This class used for processing events from CEP
 */
public interface ColleagueChangesService {

    /**
     * Consuming colleague changes events
     *
     * @param feedDeliveryMode
     * @param colleagueChangeEventPayload
     */
    void processColleagueChangeEvent(DeliveryMode feedDeliveryMode,
                                     ColleagueChangeEventPayload colleagueChangeEventPayload);

}
