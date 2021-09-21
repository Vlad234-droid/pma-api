package com.tesco.pma.colleague.cep.service;

import com.tesco.pma.colleague.cep.domain.ColleagueChangeEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    @Override
    public void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO
        log.info(String.format("Processing colleague change event %s", colleagueChangeEventPayload));
    }

}
