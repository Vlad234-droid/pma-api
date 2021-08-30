package com.tesco.pma.service.colleague;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.model.Colleague;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "colleagues")
public class ColleagueApiServiceImpl implements ColleagueApiService {

    static final String MESSAGE_PARAM_NAME_API_NAME = "apiName";
    static final String MESSAGE_PARAM_VALUE_COLLEAGUE_API = "Colleague-Api";

    private final ColleagueApiClient colleagueApiClient;
    private final NamedMessageSourceAccessor messages;

    private final CacheManager cacheManager;

    @Override
    @Cacheable
    public Colleague tryFindColleagueByUuid(UUID colleagueUuid) {
        try {
            return colleagueApiClient.findColleagueByColleagueUuid(colleagueUuid);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        } catch (RestClientException e) {
            throw colleagueApiException(e);
        }
    }

    private ExternalSystemException colleagueApiException(RestClientException restClientException) {
        return new ExternalSystemException(EXTERNAL_API_CONNECTION_ERROR.getCode(),
                messages.getMessage(EXTERNAL_API_CONNECTION_ERROR,
                        Map.of(MESSAGE_PARAM_NAME_API_NAME, MESSAGE_PARAM_VALUE_COLLEAGUE_API)), restClientException);
    }

}
