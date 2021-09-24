package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;

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
