package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.AbstractProfileTests;
import com.tesco.pma.colleague.profile.LocalTestConfig;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.service.util.ColleagueFactsApiLocalMapper;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.service.colleague.ColleagueApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS;
import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_ATTRIBUTE_NOT_FOUND;
import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest extends AbstractProfileTests {

    private static final String PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE =
            "Profile attribute name=emergencyContact for colleagueUuid=6d37262f-3a00-4706-a74b-6bf98be65765 already exists";

    private static final String PROFILE_ATTRIBUTE_NOT_FOUND_MESSAGE =
            "Profile attribute name=emergencyContact not found for colleagueUuid=6d37262f-3a00-4706-a74b-6bf98be65765";

    private static final String PROFILE_NOT_FOUND_MESSAGE =
            "Profile not found for colleagueUuid: 6d37262f-3a00-4706-a74b-6bf98be65765";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ProfileDAO profileDAO;

    @MockBean
    private ProfileAttributeDAO mockProfileDAO;

    @MockBean
    private ColleagueApiService colleagueApiService;

    @MockBean
    private ColleagueFactsApiLocalMapper colleagueFactsApiLocalMapper;

    @SpyBean
    private ProfileServiceImpl profileService;

    private final UUID colleagueUuid = COLLEAGUE_UUID_1;

    @Test
    void findProfileByColleagueUuidShouldReturnProfile() {

        when(mockProfileDAO.get(any(UUID.class)))
                .thenReturn(profileAttributes(3));

        when(profileDAO.getColleague(any(UUID.class)))
                .thenReturn(randomColleagueEntity());

        when(colleagueFactsApiLocalMapper.localToColleagueFactsApi(
                any(ColleagueEntity.class), any(UUID.class), anyBoolean()))
                .thenReturn(randomColleague());

        Optional<ColleagueProfile> profileResponse = profileService.findProfileByColleagueUuid(colleagueUuid);
        assertTrue(profileResponse.isPresent());
    }

    @Test
    void getColleagueByColleagueUuidShouldReturnColleague() {

        var colleague = randomColleagueEntity();
        when(profileDAO.getColleague(any(UUID.class)))
                .thenReturn(colleague);

        var result = profileService.getColleague(colleagueUuid);

        assertEquals(colleague, result);

        verify(profileDAO, times(1)).getColleague(colleagueUuid);
    }

    @Test
    void getColleagueByColleagueUuidWithNotFoundException() {

        when(profileDAO.getColleague(any(UUID.class)))
                .thenReturn(null);

        var exception = assertThrows(NotFoundException.class,
                () -> profileService.getColleague(colleagueUuid));

        assertEquals(PROFILE_NOT_FOUND.name(), exception.getCode());
        assertEquals(PROFILE_NOT_FOUND_MESSAGE, exception.getMessage());

        verify(profileDAO, times(1)).getColleague(colleagueUuid);
    }

    @Test
    void updateProfileAttributesShouldReturnUpdatedProfileAttributes() {

        when(mockProfileDAO.update(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.updateProfileAttributes(colleagueUuid, profileAttributes(3));

        assertFalse(results.isEmpty());
        assertEquals(3, results.size());

        verify(mockProfileDAO, times(3)).update(any(TypedAttribute.class));

    }

    @Test
    void updateProfileAttributesWithNotFoundException() {

        when(mockProfileDAO.update(any(TypedAttribute.class)))
                .thenReturn(0);

        var profileAttributes = profileAttributes(3);
        var exception = assertThrows(NotFoundException.class,
                () -> profileService.updateProfileAttributes(colleagueUuid, profileAttributes));

        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).update(any(TypedAttribute.class));

    }

    @Test
    void createProfileAttributesShouldReturnInsertedProfileAttributes() {

        when(mockProfileDAO.create(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.createProfileAttributes(colleagueUuid, profileAttributes(3));

        assertFalse(results.isEmpty());
        assertEquals(3, results.size());

        verify(mockProfileDAO, times(3)).create(any(TypedAttribute.class));

    }

    @Test
    void createProfileAttributesWithNotFoundException() {

        when(mockProfileDAO.create(any(TypedAttribute.class)))
                .thenReturn(0);

        var profileAttributes = profileAttributes(3);
        var exception = assertThrows(NotFoundException.class,
                () -> profileService.createProfileAttributes(colleagueUuid, profileAttributes));

        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).create(any(TypedAttribute.class));

    }

    @Test
    void createProfileAttributesWithNotUniqueName() {

        when(mockProfileDAO.create(any(TypedAttribute.class)))
                .thenThrow(DuplicateKeyException.class);

        var profileAttributes = profileAttributes(3);
        var exception = assertThrows(DatabaseConstraintViolationException.class,
                () -> profileService.createProfileAttributes(colleagueUuid, profileAttributes));

        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).create(any(TypedAttribute.class));

    }


    @Test
    void deleteProfileAttributesShouldReturnDeletedProfileAttributes() {

        when(mockProfileDAO.delete(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.deleteProfileAttributes(colleagueUuid, profileAttributes(3));

        assertFalse(results.isEmpty());
        assertEquals(3, results.size());

        verify(mockProfileDAO, times(3)).delete(any(TypedAttribute.class));
    }

    @Test
    void deleteProfileAttributesWithNotFoundException() {

        when(mockProfileDAO.delete(any(TypedAttribute.class)))
                .thenReturn(0);

        var profileAttributes = profileAttributes(3);
        var exception = assertThrows(NotFoundException.class,
                () -> profileService.deleteProfileAttributes(colleagueUuid, profileAttributes));

        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NOT_FOUND_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).delete(any(TypedAttribute.class));
    }

    @Test
    void getSuggestionsShouldReturnColleagueProfiles() {

        RequestQuery requestQuery = new RequestQuery();
        requestQuery.addFilters("first-name_eq", "O'Rodgers");
        requestQuery.addFilters("last-name_like", "O'Rodgers");

        when(profileDAO.findColleagueSuggestionsByFullName(any(RequestQuery.class)))
                .thenReturn(List.of());

        var result = profileService.getSuggestions(requestQuery);

        assertTrue(result.isEmpty());

        verify(profileDAO, times(1)).findColleagueSuggestionsByFullName(any(RequestQuery.class));
    }

}