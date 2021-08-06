package com.tesco.pma.service.user;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.model.Colleague;
import com.tesco.pma.service.colleague.client.model.FindColleaguesRequest;
import com.tesco.pma.service.security.SubsidiaryPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
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

    private final ColleagueApiClient colleagueApiClient;
    private final SubsidiaryPermissionService subsidiaryPermissionService;
    private final NamedMessageSourceAccessor messages;

    @Override
    public Optional<User> findUserByColleagueUuid(final UUID colleagueUuid, final Collection<UserIncludes> includes) {
        return findUserByColleagueUuidInternal(colleagueUuid, includes);
    }

    @Override
    public Optional<User> findUserByIamId(final String iamId, final Collection<UserIncludes> includes) {
        final List<Colleague> colleagues;
        try {
            colleagues = colleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build());
        } catch (RestClientException e) {
            throw colleagueApiException(e);
        }

        if (colleagues.isEmpty()) {
            return Optional.empty();
        } else if (colleagues.size() > 1) {
            throw new ExternalSystemException(COLLEAGUE_API_UNEXPECTED_RESULT.getCode(),
                    messages.getMessage(COLLEAGUE_API_UNEXPECTED_RESULT,
                            Map.of("reason", "more then one colleague found for iamId: " + iamId)));
        }
        User user = mapColleagueToUser(colleagues.iterator().next());
        return Optional.of(processIncludes(user, includes));
    }

    @Override
    public Collection<User> findUsersHasSubsidiaryPermission(final UUID subsidiaryUuid,
                                                             final String role,
                                                             final Collection<UserIncludes> includes) {
        final var colleaguesUuids = subsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid).stream()
                .filter(subsidiaryPermission -> role == null || role.equals(subsidiaryPermission.getRole()))
                .map(SubsidiaryPermission::getColleagueUuid).collect(Collectors.toSet());
        return findUsersByColleagueUuidsInternal(colleaguesUuids, includes);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tries to obtain User info from Colleague Api.
     * If a colleague not found fallback to OneLogin token User info.
     * Also extracts user roles from {@link Authentication#getAuthorities()}
     *
     * @see com.tesco.pma.configuration.security.AppendGrantedAuthoritiesBearerTokenAuthenticationMerger
     * @see UserRoleNames
     */
    @Override
    public Optional<User> findUserByAuthentication(final Authentication authentication,
                                                   final Collection<UserIncludes> includes) {
        UUID colleagueUuid;
        try {
            // colleagueUuid = IdentityToken.subject = Authentication.name
            colleagueUuid = UUID.fromString(authentication.getName());
        } catch (Exception e) {
            // in case 'name' not UUID (e.g. anonymous user, etc.)
            return Optional.empty();
        }

        User user = findUserByColleagueUuidInternal(colleagueUuid, includes).orElse(null);
        if (user == null) {
            //fallback to onelogin token if provided
            final var auth2UserAuthority = authentication.getAuthorities().stream()
                    .filter(OAuth2UserAuthority.class::isInstance)
                    .map(OAuth2UserAuthority.class::cast)
                    .findAny();

            if (auth2UserAuthority.isPresent()) {
                user = new User(colleagueUuid);
                mapOidcUserInfoToUser(new OidcUserInfo(auth2UserAuthority.get().getAttributes()), user);
                processIncludes(user, includes);
            }
        }

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

    private Optional<User> findUserByColleagueUuidInternal(final UUID colleagueUuid, final Collection<UserIncludes> includes) {
        final var users = findUsersByColleagueUuidsInternal(List.of(colleagueUuid), includes);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.iterator().next());
    }

    private Collection<User> findUsersByColleagueUuidsInternal(final Collection<UUID> colleagueUuids,
                                                               final Collection<UserIncludes> includes) {
        if (colleagueUuids.isEmpty()) {
            return Collections.emptySet();
        }
        // skip nonexistent/notfound users
        final var usersFound = colleagueUuids.stream()
                .distinct()
                .map(this::tryFindColleagueByUuid)
                .filter(Objects::nonNull)
                .map(this::mapColleagueToUser)
                .collect(Collectors.toList());

        return processIncludes(usersFound, includes);
    }

    private Collection<User> processIncludes(final Collection<User> users, final Collection<UserIncludes> includes) {
        if (!users.isEmpty() && includes != null && includes.contains(UserIncludes.SUBSIDIARY_PERMISSIONS)) {
            final var colleagueUuidsFound = users.stream()
                    .map(User::getColleagueUuid).collect(Collectors.toSet());
            final var subsidiaryPermissionsByColleagueUuid =
                    subsidiaryPermissionService.findSubsidiaryPermissionsForUsers(colleagueUuidsFound);

            users.forEach(user -> user.setSubsidiaryPermissions(subsidiaryPermissionsByColleagueUuid
                    .getOrDefault(user.getColleagueUuid(), Collections.emptySet())));
        }
        return users;
    }

    private User processIncludes(final User user, final Collection<UserIncludes> includes) {
        return processIncludes(List.of(user), includes).iterator().next();
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

    private User mapColleagueToUser(final Colleague source) {
        final var user = new User();
        user.setColleagueUuid(source.getColleagueUUID());

        final var profile = source.getProfile();
        if (source.getProfile() != null) {
            user.setTitle(profile.getTitle());
            user.setFirstName(profile.getFirstName());
            user.setMiddleName(profile.getMiddleName());
            user.setLastName(profile.getLastName());
            user.setGender(profile.getGender());
        }

        final var contact = source.getContact();
        if (contact != null) {
            user.setEmail(contact.getEmail());
        }

        return user;
    }

    private User mapOidcUserInfoToUser(OidcUserInfo source, User target) {
        target.setFirstName(source.getGivenName());
        target.setMiddleName(source.getMiddleName());
        target.setGender(source.getGender());
        target.setLastName(source.getFamilyName());
        target.setEmail(source.getEmail());
        return target;
    }


    private ExternalSystemException colleagueApiException(RestClientException restClientException) {
        return new ExternalSystemException(EXTERNAL_API_CONNECTION_ERROR.getCode(),
                messages.getMessage(EXTERNAL_API_CONNECTION_ERROR,
                        Map.of(MESSAGE_PARAM_NAME_API_NAME, MESSAGE_PARAM_VALUE_COLLEAGUE_API)), restClientException);
    }
}
