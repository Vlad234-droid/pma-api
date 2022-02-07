package com.tesco.pma.cep.v2.service;

import com.tesco.pma.cep.v2.domain.ColleagueChangeEventPayload;

/**
 * This class used for processing events from CEP
 */
public interface ColleagueChangesService {

    /**
     * Consuming colleague changes events
     *  @param feedId
     * @param colleagueChangeEventPayload
     */
    void processColleagueChangeEvent(String feedId,
                                     ColleagueChangeEventPayload colleagueChangeEventPayload);

}
