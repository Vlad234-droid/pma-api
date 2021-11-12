package com.tesco.pma.user;

import com.tesco.pma.api.User;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.user.util.RolesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
import java.util.HashSet;
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

    private final ConfigEntryService configEntryService;
    private final ColleagueApiClient colleagueApiClient;
    private final UserManagementService userManagementService;
    private final NamedMessageSourceAccessor messages;
    private final RolesMapper rolesMapper;

    @Override
    public Optional<User> findUserByColleagueUuid(final UUID colleagueUuid) {
        return findUserByColleagueUuidInternal(colleagueUuid);
    }

    @Override
    public Optional<User> findUserByIamId(final String iamId) {
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
        var user = mapColleagueToUser(colleagues.iterator().next());
        return Optional.of(user);
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
        if (user == null) {
            //fallback to onelogin token if provided
            final var auth2UserAuthority = authentication.getAuthorities().stream()
                    .filter(OAuth2UserAuthority.class::isInstance)
                    .map(OAuth2UserAuthority.class::cast)
                    .findAny();

            if (auth2UserAuthority.isPresent()) {
                user = new User(colleagueUuid);
                mapOidcUserInfoToUser(new OidcUserInfo(auth2UserAuthority.get().getAttributes()), user);
            }
        }

        if (user != null) {
            // First attempt - try to find roles in authentication authorities
            Collection<String> roles = new HashSet<>(findRolesInAuthentication(authentication));

            // Second attempt - try to find roles in account storage
            roles.addAll(findRolesInAccountStorage(colleagueUuid));

            user.setRoles(roles);
        }

        return Optional.ofNullable(user);
    }

    private Collection<String> findRolesInAuthentication(final Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(grantedAuthority -> grantedAuthority.replace("ROLE_", ""))
                .filter(UserRoleNames.ALL::contains)
                .collect(Collectors.toList());
    }

    private Collection<String> findRolesInAccountStorage(UUID colleagueUuid) {
        com.tesco.pma.organisation.api.Colleague colleague = configEntryService.getColleague(colleagueUuid);
        if (colleague != null && colleague.getIamId() != null) {
            Account account = userManagementService.findAccountByIamId(colleague.getIamId());
            if (account != null) {
                Collection<Role> roles = account.getRoles();
                return roles.stream()
                        .map(role -> rolesMapper.findRoleByCode(role.getCode()))
                        .map(role -> role.replaceAll("ROLE_", ""))
                        .filter(UserRoleNames.ALL::contains)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptySet();
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
                .map(this::tryFindColleagueByUuid)
                .filter(Objects::nonNull)
                .map(this::mapColleagueToUser)
                .collect(Collectors.toList());
    }

    private Colleague tryFindColleagueByUuid(UUID colleagueUuid) {
        // First attempt - try to find in local storage
        com.tesco.pma.organisation.api.Colleague oc = configEntryService.getColleague(colleagueUuid);
        if (oc != null) {
            return mapLocalColleagueToColleague(oc);
        }

        // Second attempt - try to find with using external Colleague Facts API
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

    private Colleague mapLocalColleagueToColleague(final com.tesco.pma.organisation.api.Colleague localColleague) {
        var colleague = new Colleague();
        colleague.setColleagueUUID(localColleague.getUuid());

        var profile = new Profile();
        BeanUtils.copyProperties(localColleague, profile);
        colleague.setProfile(profile);

        var contact = new Contact();
        BeanUtils.copyProperties(localColleague, contact);
        colleague.setContact(contact);

        return colleague;
    }

    private ExternalSystemException colleagueApiException(RestClientException restClientException) {
        return new ExternalSystemException(EXTERNAL_API_CONNECTION_ERROR.getCode(),
                messages.getMessage(EXTERNAL_API_CONNECTION_ERROR,
                        Map.of(MESSAGE_PARAM_NAME_API_NAME, MESSAGE_PARAM_VALUE_COLLEAGUE_API)), restClientException);
    }
}
