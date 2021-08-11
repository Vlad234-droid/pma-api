package com.tesco.pma.profile.service;

import com.tesco.pma.profile.domain.Profile;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.profile.dao.ProfileDAO;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.user.UserIncludes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ProfileService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileDAO profileDAO;
    private final ColleagueApiClient colleagueApiClient;
    private final NamedMessageSourceAccessor messages;

    @Override
    public Optional<Profile> findProfileByColleagueUuid(UUID colleagueUuid) {
        return Optional.of(profileDAO.get(colleagueUuid));
    }

}
