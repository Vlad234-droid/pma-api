package com.tesco.pma.user;

import com.tesco.pma.api.User;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.exception.ErrorCodes.COLLEAGUE_API_UNEXPECTED_RESULT;
import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;

/**
 * Implementation of {@link UserService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    static final String MESSAGE_PARAM_NAME_API_NAME = "apiName";
    static final String MESSAGE_PARAM_VALUE_COLLEAGUE_API = "Colleague-Api";

    private final ProfileService profileService;
    private final ColleagueApiClient colleagueApiClient;
    private final NamedMessageSourceAccessor messages;

    @Override
    public Optional<User> findUserByColleagueUuid(final UUID colleagueUuid) {
        return findUserByColleagueUuidInternal(colleagueUuid);
    }

    @Override
    public Optional<User> findUserByIamId(final String iamId) {
        var colleagueProfile = tryFindColleagueProfileByIamId(iamId);
        if (colleagueProfile != null) {
            return Optional.of(mapColleagueProfileToUser(colleagueProfile));
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tries to obtain User info from Colleague Api.
     * Also extracts user roles from {@link Authentication#getAuthorities()}
     *
     * @see UserRoleNames
     */
    @Override
    public Optional<User> findUserByAuthentication(final Authentication authentication) {
        UUID colleagueUuid;
        try {
            // colleagueUuid = IdentityToken.subject = Authentication.name
            colleagueUuid = UUID.fromString(authentication.getName());
        } catch (Exception e) {
            // in case 'name' not UUID (e.g. anonymous user, etc.)
            return Optional.empty();
        }

        var user = findUserByColleagueUuidInternal(colleagueUuid).orElse(null);
        if (user != null) {
            final var roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(grantedAuthority -> grantedAuthority.replace("ROLE_", ""))
                    .filter(UserRoleNames.ALL::contains)
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }

        return Optional.ofNullable(user);
    }

    private ColleagueProfile tryFindColleagueProfileByIamId(String iamId) {
        // First attempt - try to find in local storage
        var optionalColleagueProfile = profileService.findProfileByColleagueIamId(iamId);
        if (optionalColleagueProfile.isPresent()) {
            return optionalColleagueProfile.get();
        }

        // Second attempt - try to find with using external Colleague Facts API
        final List<Colleague> colleagues;
        try {
            colleagues = colleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build());
        } catch (RestClientException e) {
            throw colleagueApiException(e);
        }

        if (colleagues.isEmpty()) {
            return null;
        } else if (colleagues.size() > 1) {
            throw new ExternalSystemException(COLLEAGUE_API_UNEXPECTED_RESULT.getCode(),
                    messages.getMessage(COLLEAGUE_API_UNEXPECTED_RESULT,
                            Map.of("reason", "more then one colleague found for iamId: " + iamId)));
        }

        var colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(colleagues.iterator().next());

        return colleagueProfile;
    }

    private Optional<User> findUserByColleagueUuidInternal(final UUID colleagueUuid) {
        final var users = findUsersByColleagueUuidsInternal(List.of(colleagueUuid));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.iterator().next());
    }

    private Collection<User> findUsersByColleagueUuidsInternal(final Collection<UUID> colleagueUuids) {
        if (colleagueUuids.isEmpty()) {
            return Collections.emptySet();
        }
        // skip nonexistent/notfound users
        return colleagueUuids.stream()
                .distinct()
                .map(this::tryFindColleagueProfileByUuid)
                .filter(Objects::nonNull)
                .map(this::mapColleagueProfileToUser)
                .collect(Collectors.toList());
    }

    private ColleagueProfile tryFindColleagueProfileByUuid(UUID colleagueUuid) {
        // First attempt - try to find in local storage
        var optionalColleagueProfile = profileService.findProfileByColleagueUuid(colleagueUuid);
        if (optionalColleagueProfile.isPresent()) {
            return optionalColleagueProfile.get();
        }

        // Second attempt - try to find with using external Colleague Facts API
        try {
            var colleague = colleagueApiClient.findColleagueByColleagueUuid(colleagueUuid);
            var colleagueProfile = new ColleagueProfile();
            colleagueProfile.setColleague(colleague);
            return colleagueProfile;
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        } catch (RestClientException e) {
            throw colleagueApiException(e);
        }
    }

    private User mapColleagueProfileToUser(final ColleagueProfile source) {
        final var user = new User();
        user.setColleague(source.getColleague());
        user.setProfileAttributes(source.getProfileAttributes());
        return user;
    }

    private ExternalSystemException colleagueApiException(RestClientException restClientException) {
        return new ExternalSystemException(EXTERNAL_API_CONNECTION_ERROR.getCode(),
                messages.getMessage(EXTERNAL_API_CONNECTION_ERROR,
                        Map.of(MESSAGE_PARAM_NAME_API_NAME, MESSAGE_PARAM_VALUE_COLLEAGUE_API)), restClientException);
    }
}
