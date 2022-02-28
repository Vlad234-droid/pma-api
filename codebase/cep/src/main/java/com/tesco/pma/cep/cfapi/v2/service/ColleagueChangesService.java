package com.tesco.pma.cep.cfapi.v2.service;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;

/**
 * This class used for processing events from CEP
 */
public interface ColleagueChangesService {

    /**
     * Consuming colleague changes events
     * @param colleagueChangeEventPayload
     */
    void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload);

}
