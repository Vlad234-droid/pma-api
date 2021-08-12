package com.tesco.pma.profile.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.Profile;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.model.Colleague;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    static final String MESSAGE_PARAM_NAME_API_NAME = "apiName";
    static final String MESSAGE_PARAM_VALUE_COLLEAGUE_API = "Colleague-Api";

    private final ProfileAttributeDAO profileDAO;
    private final ColleagueApiClient colleagueApiClient;
    private final NamedMessageSourceAccessor messages;

    @Override
    public Optional<Profile> findProfileByColleagueUuid(UUID colleagueUuid) {

        List<ProfileAttribute> profileAttributes = profileDAO.get(colleagueUuid);

        Colleague colleague = tryFindColleagueByUuid(colleagueUuid);

        Profile profile = new Profile();
        profile.setColleague(colleague);
        profile.setProfileAttributes(profileAttributes);

        return Optional.of(profile);

    }

    private Colleague tryFindColleagueByUuid(UUID colleagueUuid) {
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
