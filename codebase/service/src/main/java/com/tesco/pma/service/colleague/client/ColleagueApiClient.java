package com.tesco.pma.service.colleague.client;

import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.FindColleaguesRequest;

import java.util.List;
import java.util.UUID;

/**
 * Colleague Api v2 client interface.
 */
public interface ColleagueApiClient {

    Colleague findColleagueByColleagueUuid(UUID colleagueUuid);

    List<Colleague> findColleagues(FindColleaguesRequest findColleaguesRequest);

}
