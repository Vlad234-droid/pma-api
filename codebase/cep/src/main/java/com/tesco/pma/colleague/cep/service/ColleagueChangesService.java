package com.tesco.pma.colleague.cep.service;

import com.tesco.pma.colleague.cep.domain.ColleagueChangeEventPayload;

public interface ColleagueChangesService {

    /**
     * Consuming colleague changes events
     *
     * @param colleagueChangeEventPayload
     */
    void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload);

}
