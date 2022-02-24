package com.tesco.pma.service.colleague.client;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.ColleagueList;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.USER_NOT_FOUND;

/**
 * Implementation of {@link ColleagueApiClient}.
 *
 * <p>Uses {@link RestTemplate}.
 */
public class RestTemplateBasedColleagueApiClient implements ColleagueApiClient {
    static final String EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME = "externalSystems.iam.id";

    private final RestTemplate restTemplate;
    private final UriBuilderFactory uriBuilderFactory;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public RestTemplateBasedColleagueApiClient(String baseUrl, RestTemplate restTemplate,
                                               NamedMessageSourceAccessor messageSourceAccessor) {
        this.restTemplate = restTemplate;
        this.uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        this.messageSourceAccessor = messageSourceAccessor;
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
        final var colleagueList = Optional.ofNullable(restTemplate.getForObject(uriBuilder.build(), ColleagueList.class))
                .orElseThrow(() -> notFound(EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME, findColleaguesRequest.getIamId()));
        return colleagueList.getColleagues();
    }

    private NotFoundException notFound(String paramName, String paramValue) {
        return new NotFoundException(USER_NOT_FOUND.getCode(), messageSourceAccessor.getMessage(USER_NOT_FOUND,
                Map.of("param_name", paramName, "param_value", paramValue)));
    }
}
