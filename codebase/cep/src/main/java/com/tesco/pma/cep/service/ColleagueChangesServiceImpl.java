package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.DeliveryMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    @Override
    public void processColleagueChangeEvent(DeliveryMode feedDeliveryMode,
                                            ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ...
        log.info(String.format("Processing colleague change event %s for feed delivery mode %s",
                colleagueChangeEventPayload, feedDeliveryMode));
    }

}
