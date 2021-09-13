package com.tesco.pma.service.user;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.service.security.SubsidiaryPermissionService;
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
import java.util.Set;
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
    private ColleagueApiClient mockColleagueApiClient;
    @Mock
    private SubsidiaryPermissionService mockSubsidiaryPermissionService;
    @Mock
    private NamedMessageSourceAccessor mockNamedMessageSourceAccessor;

    @BeforeEach
    void setUp() {
        instance = new UserServiceImpl(mockColleagueApiClient, mockSubsidiaryPermissionService, mockNamedMessageSourceAccessor);
    }

    @Test
    void findUserByColleagueUuidFound() {
        final var colleague = randomColleague();
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByColleagueUuid(colleague.getColleagueUUID(), Collections.emptySet());

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByColleagueUuidNotFound() {
        final var colleague = randomColleague();
        findColleagueByColleagueUuidNotFoundCall(colleague.getColleagueUUID());

        final var res = instance.findUserByColleagueUuid(colleague.getColleagueUUID(), Collections.emptySet());

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByColleagueUuidFetchSubsidiaryPermissions() {
        final var colleague = randomColleague();
        final var subsidiaryPermissions = RANDOM.objects(SubsidiaryPermission.class, 3).collect(toList());
        final var colleagueUuid = colleague.getColleagueUUID();
        findColleagueByColleagueUuidFoundCall(colleague);
        findSubsidiaryPermissionsCall(colleagueUuid, subsidiaryPermissions);

        final var res = instance.findUserByColleagueUuid(colleagueUuid,
                Set.of(UserIncludes.SUBSIDIARY_PERMISSIONS));

        assertThat(res).hasValueSatisfying(hasSubsidiaryPermissions(subsidiaryPermissions));
    }

    @Test
    void findUserByColleagueUuidColleagueApiException() {
        final var colleague = randomColleague();
        final var restClientException = RANDOM.nextObject(RestClientException.class);
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleague.getColleagueUUID()))
                .thenThrow(restClientException);

        assertThatCode(() -> instance.findUserByColleagueUuid(colleague.getColleagueUUID(), Collections.emptySet()))
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

        final var res = instance.findUserByIamId(iamId, Collections.emptySet());

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByIamIdNotFound() {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(Collections.emptyList());

        final var res = instance.findUserByIamId(iamId, Collections.emptySet());

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByIamIdFoundMoreThenOneThrowsException() {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(List.of(randomColleague(), randomColleague()));

        assertThatCode(() -> instance.findUserByIamId(iamId, Collections.emptySet()))
                .isInstanceOf(ExternalSystemException.class)
                .hasNoCause();

        verify(mockNamedMessageSourceAccessor).getMessage(COLLEAGUE_API_UNEXPECTED_RESULT,
                Map.of("reason", "more then one colleague found for iamId: " + iamId));
    }

    @Test
    void findUserByIamIdFetchSubsidiaryPermissions() {
        final var iamId = RANDOM.nextObject(String.class);
        final var colleague = randomColleague();
        final var subsidiaryPermissions = RANDOM.objects(SubsidiaryPermission.class, 3).collect(toList());
        final var colleagueUuid = colleague.getColleagueUUID();
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenReturn(List.of(colleague));
        findSubsidiaryPermissionsCall(colleagueUuid, subsidiaryPermissions);

        final var res = instance.findUserByIamId(iamId, Set.of(UserIncludes.SUBSIDIARY_PERMISSIONS));

        assertThat(res).hasValueSatisfying(hasSubsidiaryPermissions(subsidiaryPermissions));
    }

    @Test
    void findUserByIamIdColleagueApiException() {
        final var iamId = RANDOM.nextObject(String.class);
        final var restClientException = RANDOM.nextObject(RestClientException.class);
        when(mockColleagueApiClient.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build()))
                .thenThrow(restClientException);

        assertThatCode(() -> instance.findUserByIamId(iamId, Collections.emptySet()))
                .hasCause(restClientException)
                .isInstanceOf(ExternalSystemException.class);

        verify(mockNamedMessageSourceAccessor).getMessage(EXTERNAL_API_CONNECTION_ERROR,
                Map.of(UserServiceImpl.MESSAGE_PARAM_NAME_API_NAME, UserServiceImpl.MESSAGE_PARAM_VALUE_COLLEAGUE_API));
    }

    @Test
    void findUsersHasSubsidiaryPermissionPermissionsNotFound() {
        final var subsidiaryUuid = randomUUID();
        final var role = RANDOM.nextObject(String.class);
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid)).thenReturn(Collections.emptySet());

        final var res = instance.findUsersHasSubsidiaryPermission(subsidiaryUuid, role, Collections.emptySet());

        assertThat(res).isEmpty();
    }

    @Test
    void findUsersHasSubsidiaryPermissionPermissionsFoundWithProvidedRole() {
        final var subsidiaryUuid = randomUUID();
        final var colleagueWithMatchedRole = randomColleague();
        final var colleagueWithNotMatchedRole = randomColleague();
        final var roleTarget = RANDOM.nextObject(String.class);
        final var roleNotMatched = RANDOM.nextObject(String.class);
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid))
                .thenReturn(List.of(
                        SubsidiaryPermission.of(colleagueWithMatchedRole.getColleagueUUID(), subsidiaryUuid, roleTarget),
                        SubsidiaryPermission.of(colleagueWithNotMatchedRole.getColleagueUUID(), subsidiaryUuid, roleNotMatched)
                ));

        findColleagueByColleagueUuidFoundCall(colleagueWithMatchedRole);

        final var res = instance.findUsersHasSubsidiaryPermission(subsidiaryUuid, roleTarget, Collections.emptySet());

        assertThat(res).hasSize(1)
                .anySatisfy(colleagueProperlyMapped(colleagueWithMatchedRole));
    }

    @Test
    void findUsersHasSubsidiaryPermissionPermissionsFoundForAnyRole() {
        final var subsidiaryUuid = randomUUID();
        final var colleague1 = randomColleague();
        final var colleague2 = randomColleague();
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid))
                .thenReturn(List.of(
                        SubsidiaryPermission.of(colleague1.getColleagueUUID(), subsidiaryUuid, RANDOM.nextObject(String.class)),
                        SubsidiaryPermission.of(colleague2.getColleagueUUID(), subsidiaryUuid, RANDOM.nextObject(String.class))
                ));

        findColleagueByColleagueUuidFoundCall(colleague1);
        findColleagueByColleagueUuidFoundCall(colleague2);

        final var res = instance.findUsersHasSubsidiaryPermission(subsidiaryUuid, null, Collections.emptySet());

        assertThat(res).hasSize(2)
                .anySatisfy(colleagueProperlyMapped(colleague1))
                .anySatisfy(colleagueProperlyMapped(colleague2));
    }

    @Test
    void findUserByAuthenticationAuthenticationNameNotUUIDReturnsEmpty() {
        final var auth = new TestingAuthenticationToken("not-uuid", CREDENTIALS);

        final var res = instance.findUserByAuthentication(auth, Collections.emptySet());

        assertThat(res).isEmpty();
    }

    @Test
    void findUserByAuthenticationFoundInColleagueApi() {
        final var colleague = randomColleague();
        final var auth = new TestingAuthenticationToken(colleague.getColleagueUUID().toString(), CREDENTIALS);
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByAuthentication(auth, Collections.emptySet());

        assertThat(res).hasValueSatisfying(colleagueProperlyMapped(colleague));
    }

    @Test
    void findUserByAuthenticationNotFoundInColleagueApiThenFallbackToOidcTokenThatPresent() {
        final var colleagueUuid = randomUUID();
        final var expectedUser = new User(colleagueUuid);
        expectedUser.setFirstName(RANDOM.nextObject(String.class));
        expectedUser.setLastName(RANDOM.nextObject(String.class));
        expectedUser.setMiddleName(RANDOM.nextObject(String.class));
        expectedUser.setGender(RANDOM.nextObject(String.class));
        expectedUser.setEmail(RANDOM.nextObject(String.class));
        final var oAuth2UserAuthority = new OAuth2UserAuthority(Map.of(
                StandardClaimNames.GIVEN_NAME, expectedUser.getFirstName(),
                StandardClaimNames.FAMILY_NAME, expectedUser.getLastName(),
                StandardClaimNames.MIDDLE_NAME, expectedUser.getMiddleName(),
                StandardClaimNames.GENDER, expectedUser.getGender(),
                StandardClaimNames.EMAIL, expectedUser.getEmail()
        ));
        final var auth = new TestingAuthenticationToken(colleagueUuid.toString(), CREDENTIALS, List.of(oAuth2UserAuthority));
        findColleagueByColleagueUuidNotFoundCall(colleagueUuid);

        final var res = instance.findUserByAuthentication(auth, Collections.emptySet());

        assertThat(res).get().usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void findUserByAuthenticationNotFoundInColleagueApiAndNoOidcTokenThenReturnsEmpty() {
        final var colleagueUuid = randomUUID();

        final var auth = new TestingAuthenticationToken(colleagueUuid.toString(), CREDENTIALS);
        findColleagueByColleagueUuidNotFoundCall(colleagueUuid);

        final var res = instance.findUserByAuthentication(auth, Collections.emptySet());

        assertThat(res).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideAuthoritiesAndExpectedRoles")
    void findUserByAuthenticationExtractUserRolesRoles(Collection<String> authorities, Collection<String> expectedRoles) {
        final var colleague = randomColleague();

        final var auth = new TestingAuthenticationToken(colleague.getColleagueUUID().toString(), CREDENTIALS,
                authorities.toArray(String[]::new));
        findColleagueByColleagueUuidFoundCall(colleague);

        final var res = instance.findUserByAuthentication(auth, Collections.emptySet());

        assertThat(res).hasValueSatisfying(user -> assertThat(user.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoles));
    }

    private Colleague randomColleague() {
        return RANDOM.nextObject(Colleague.class);
    }

    private void findColleagueByColleagueUuidFoundCall(Colleague colleague) {
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleague.getColleagueUUID())).thenReturn(colleague);
    }

    private void findColleagueByColleagueUuidNotFoundCall(UUID colleagueUuid) {
        when(mockColleagueApiClient.findColleagueByColleagueUuid(colleagueUuid))
                .thenThrow(RANDOM.nextObject(HttpClientErrorException.NotFound.class));
    }

    private void findSubsidiaryPermissionsCall(UUID colleagueUuid, Collection<SubsidiaryPermission> subsidiaryPermissions) {
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForUsers(Set.of(colleagueUuid)))
                .thenReturn(Map.of(colleagueUuid, subsidiaryPermissions));
    }

    private Consumer<User> hasSubsidiaryPermissions(Collection<SubsidiaryPermission> subsidiaryPermissions) {
        return user -> assertThat(user.getSubsidiaryPermissions()).containsExactlyInAnyOrderElementsOf(subsidiaryPermissions);
    }

    private Consumer<User> colleagueProperlyMapped(Colleague colleague) {
        return user -> assertThat(user)
                .returns(colleague.getColleagueUUID(), from(User::getColleagueUuid))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getTitle).orElse(null), from(User::getTitle))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getFirstName).orElse(null), from(User::getFirstName))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getMiddleName).orElse(null), from(User::getMiddleName))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getLastName).orElse(null), from(User::getLastName))
                .returns(Optional.ofNullable(colleague.getProfile()).map(Profile::getGender).orElse(null), from(User::getGender))
                .returns(Optional.ofNullable(colleague.getContact()).map(Contact::getEmail).orElse(null), from(User::getEmail));
    }

    private static Stream<Arguments> provideAuthoritiesAndExpectedRoles() {
        return Stream.of(
                arguments(UserRoleNames.ALL.stream().map("ROLE_"::concat).collect(toList()), UserRoleNames.ALL),
                arguments(UserRoleNames.ALL, UserRoleNames.ALL),
                arguments(RANDOM.objects(String.class, 5).collect(toList()), Collections.emptyList())
        );
    }
}