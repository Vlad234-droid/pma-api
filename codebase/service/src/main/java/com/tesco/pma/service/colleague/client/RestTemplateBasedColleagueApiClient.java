package com.tesco.pma.service.colleague.client;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.ColleagueList;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Implementation of {@link ColleagueApiClient}.
 *
 * <p>Uses {@link RestTemplate}.
 */
public class RestTemplateBasedColleagueApiClient implements ColleagueApiClient {
    static final String EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME = "externalSystems.iam.id";

    private final RestTemplate restTemplate;
    private final UriBuilderFactory uriBuilderFactory;

    public RestTemplateBasedColleagueApiClient(String baseUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
    }

    @Override
    public Colleague findColleagueByColleagueUuid(UUID colleagueUuid) {
        final var uriBuilder = uriBuilderFactory.builder()
                .path("/{colleagueUUID}");

        return restTemplate.getForObject(uriBuilder.build(colleagueUuid), Colleague.class);
    }

    @Override
    public List<Colleague> findColleagues(FindColleaguesRequest findColleaguesRequest) {
        final var uriBuilder = uriBuilderFactory.builder()
                .queryParamIfPresent(EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME, Optional.ofNullable(findColleaguesRequest.getIamId()));
        return restTemplate.getForObject(uriBuilder.build(), ColleagueList.class).getColleagues();
    }

}
