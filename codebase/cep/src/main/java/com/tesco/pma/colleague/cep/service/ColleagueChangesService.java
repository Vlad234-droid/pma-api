package com.tesco.pma.colleague.cep.service;

import com.tesco.pma.colleague.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.colleague.cep.domain.DeliveryMode;

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
