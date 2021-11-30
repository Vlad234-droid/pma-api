package com.tesco.pma.user;

import com.tesco.pma.api.User;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.tesco.pma.exception.ErrorCodes.COLLEAGUE_API_UNEXPECTED_RESULT;
import static com.tesco.pma.exception.ErrorCodes.EXTERNAL_API_CONNECTION_ERROR;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final EasyRandom RANDOM = new EasyRandom();
    private static final String CREDENTIALS = "creds";

    private UserServiceImpl instance;

    @Mock
    private ProfileService mockProfileService;
    @Mock
    private ColleagueApiClient mockColleagueApiClient;
    @Mock
    private NamedMessageSourceAccessor mockNamedMessageSourceAccessor;

    @BeforeEach
    void setUp() {
        instance = new UserServiceImpl(mockProfileService, mockColleagueApiClient, mockNamedMessageSourceAccessor);
    }

    @Test
    void findUserByColleagueUuidFound() {
        final var colleague = randomColleague();
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByColleagueUuid(colleague.getColleagueUUID());

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByColleagueUuidNotFound() {
        final var colleague = randomColleague();
        findColleagueByColleagueUuidNotFoundCall(colleague.getColleagueUUID());

        final var res = instance.findUserByColleagueUuid(colleague.getColleagueUUID());

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByColleagueUuidColleagueApiException() {
        final var colleague = randomColleague();
        final var restClientException = RANDOM.nextObject(RestClientException.class);
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleague.getColleagueUUID()))
                .thenThrow(restClientException);
        when(mockProfileService.getColleague(colleague.getColleagueUUID()))
                .thenThrow(RANDOM.nextObject(NotFoundException.class));

        assertThatCode(() -> instance.findUserByColleagueUuid(colleague.getColleagueUUID()))
                .hasCause(restClientException)
                .isInstanceOf(ExternalSystemException.class);

        verify(mockNamedMessageSourceAccessor).getMessage(EXTERNAL_API_CONNECTION_ERROR,
                Map.of(UserServiceImpl.MESSAGE_PARAM_NAME_API_NAME, UserServiceImpl.MESSAGE_PARAM_VALUE_COLLEAGUE_API));
    }

    @Test
    void findUserByIamIdFound() {
        final var iamId = RANDOM.nextObject(String.class);
        final var colleague = randomColleague();
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(List.of(colleague));

        final var res = instance.findUserByIamId(iamId);

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByIamIdNotFound() {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(Collections.emptyList());

        final var res = instance.findUserByIamId(iamId);

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByIamIdFoundMoreThenOneThrowsException() {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(List.of(randomColleague(), randomColleague()));

        assertThatCode(() -> instance.findUserByIamId(iamId))
                .isInstanceOf(ExternalSystemException.class)
                .hasNoCause();

        verify(mockNamedMessageSourceAccessor).getMessage(COLLEAGUE_API_UNEXPECTED_RESULT,
                Map.of("reason", "more then one colleague found for iamId: " + iamId));
    }

    @Test
    void findUserByIamIdColleagueApiException() {
        final var iamId = RANDOM.nextObject(String.class);
        final var restClientException = RANDOM.nextObject(RestClientException.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenThrow(restClientException);

        assertThatCode(() -> instance.findUserByIamId(iamId))
                .hasCause(restClientException)
                .isInstanceOf(ExternalSystemException.class);

        verify(mockNamedMessageSourceAccessor).getMessage(EXTERNAL_API_CONNECTION_ERROR,
                Map.of(UserServiceImpl.MESSAGE_PARAM_NAME_API_NAME, UserServiceImpl.MESSAGE_PARAM_VALUE_COLLEAGUE_API));
    }

    @Test
    void findUserByAuthenticationAuthenticationNameNotUUIDReturnsEmpty() {
        final var auth = new TestingAuthenticationToken("not-uuid", CREDENTIALS);

        final var res = instance.findUserByAuthentication(auth);

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByAuthenticationFoundInColleagueApi() {
        final var colleague = randomColleague();
        final var auth = new TestingAuthenticationToken(colleague.getColleagueUUID().toString(), CREDENTIALS);
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByAuthentication(auth);

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByAuthenticationNotFoundInColleagueApiThenFallbackToOidcTokenThatPresent() {
        final var colleagueUuid = randomUUID();
        final var expectedUser = new User();
        var profile = new Profile();
        profile.setFirstName(RANDOM.nextObject(String.class));
        profile.setMiddleName(RANDOM.nextObject(String.class));
        profile.setGender(RANDOM.nextObject(String.class));
        profile.setLastName(RANDOM.nextObject(String.class));
        var contact = new Contact();
        contact.setEmail(RANDOM.nextObject(String.class));
        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUuid);
        colleague.setProfile(profile);
        colleague.setContact(contact);
        expectedUser.setColleague(colleague);

        final var oAuth2UserAuthority = new OAuth2UserAuthority(Map.of(
                StandardClaimNames.GIVEN_NAME, expectedUser.getColleague().getProfile().getFirstName(),
                StandardClaimNames.FAMILY_NAME, expectedUser.getColleague().getProfile().getLastName(),
                StandardClaimNames.MIDDLE_NAME, expectedUser.getColleague().getProfile().getMiddleName(),
                StandardClaimNames.GENDER, expectedUser.getColleague().getProfile().getGender(),
                StandardClaimNames.EMAIL, expectedUser.getColleague().getContact().getEmail()
        ));
        final var auth = new TestingAuthenticationToken(colleagueUuid.toString(), CREDENTIALS, List.of(oAuth2UserAuthority));
        findColleagueByColleagueUuidNotFoundCall(colleagueUuid);

        final var res = instance.findUserByAuthentication(auth);

        assertThat(res).get().usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void findUserByAuthenticationNotFoundInColleagueApiAndNoOidcTokenThenReturnsEmpty() {
        final var colleagueUuid = randomUUID();

        final var auth = new TestingAuthenticationToken(colleagueUuid.toString(), CREDENTIALS);
        findColleagueByColleagueUuidNotFoundCall(colleagueUuid);

        final var res = instance.findUserByAuthentication(auth);

        assertThat(res).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideAuthoritiesAndExpectedRoles")
    void findUserByAuthenticationExtractUserRolesRoles(Collection<String> authorities, Collection<String> expectedRoles) {
        final var colleague = randomColleague();

        final var auth = new TestingAuthenticationToken(colleague.getColleagueUUID().toString(), CREDENTIALS,
                authorities.toArray(String[]::new));
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByAuthentication(auth);

        assertThat(res).hasValueSatisfying(user -> assertThat(user.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoles));
    }

    private Colleague randomColleague() {
        return RANDOM.nextObject(Colleague.class);
    }

    private void findColleagueByColleagueUuidFoundCall(Colleague colleague) {
        when(mockProfileService.getColleague(colleague.getColleagueUUID()))
                .thenThrow(RANDOM.nextObject(NotFoundException.class));
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleague.getColleagueUUID())).thenReturn(colleague);
    }

    private void findColleagueByColleagueUuidNotFoundCall(UUID colleagueUuid) {
        when(mockProfileService.getColleague(colleagueUuid))
                .thenThrow(RANDOM.nextObject(NotFoundException.class));
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleagueUuid))
                .thenThrow(RANDOM.nextObject(HttpClientErrorException.NotFound.class));
    }

    private Consumer<User> colleagueProperlyMapped(Colleague colleague) {
        return user -> assertThat(user)
                .returns(colleague.getColleagueUUID(),
                        from(u -> u.getColleague().getColleagueUUID()))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getTitle).orElse(null),
                        from(u -> u.getColleague().getProfile().getTitle()))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getFirstName).orElse(null),
                        from(u -> u.getColleague().getProfile().getFirstName()))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getMiddleName).orElse(null),
                        from(u -> u.getColleague().getProfile().getMiddleName()))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getLastName).orElse(null),
                        from(u -> u.getColleague().getProfile().getLastName()))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getGender).orElse(null),
                        from(u -> u.getColleague().getProfile().getGender()))
                .returns(Optional.ofNullable(colleague.getContact()).map(Contact::getEmail).orElse(null),
                        from(u -> u.getColleague().getContact().getEmail()));
    }

    private static Stream<Arguments> provideAuthoritiesAndExpectedRoles() {
        return Stream.of(
                arguments(UserRoleNames.ALL.stream().map("ROLE_"::concat).collect(toList()), UserRoleNames.ALL),
                arguments(UserRoleNames.ALL, UserRoleNames.ALL),
                arguments(RANDOM.objects(String.class, 5).collect(toList()), Collections.emptyList())
        );
    }
}