package com.tesco.pma.service.colleague;

import com.tesco.pma.service.colleague.client.model.Colleague;
import org.springframework.cache.annotation.Cacheable;

import java.util.UUID;

public interface ColleagueApiService {

    /**
     * Find colleague by UUID
     * @param colleagueUuid
     * @return Colleague information
     */
    Colleague findColleagueByUuid(UUID colleagueUuid);

}
