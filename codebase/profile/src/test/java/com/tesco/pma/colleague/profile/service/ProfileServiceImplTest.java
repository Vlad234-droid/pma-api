package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.colleague.profile.AbstractTests;
import com.tesco.pma.colleague.profile.LocalTestConfig;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.colleague.profile.rest.model.AggregatedColleagueResponse;
import com.tesco.pma.service.colleague.ColleagueApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest extends AbstractTests {

    private static final String PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE =
            "Profile attribute name=emergencyContact for colleagueUuid=6d37262f-3a00-4706-a74b-6bf98be65765 already exists";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @Mock
    private ProfileAttributeDAO mockProfileDAO;

    @Mock
    private ColleagueApiService mockColleagueApiService;

//    @InjectMocks
    private ProfileServiceImpl mockProfileService;

    @BeforeEach
    void setUp() {
        mockProfileService = new ProfileServiceImpl(mockProfileDAO, mockColleagueApiService, messages);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findProfileByColleagueUuidShouldReturnProfile() {
        final var colleagueUuid = randomUUID();

        when(mockProfileDAO.get(any(UUID.class)))
                .thenReturn(profileAttributes(3));

        when(mockColleagueApiService.tryFindColleagueByUuid(any(UUID.class)))
                .thenReturn(randomColleague());

        Optional<AggregatedColleagueResponse> profileResponse = mockProfileService.findProfileByColleagueUuid(colleagueUuid);
        assertThat(profileResponse).isPresent();

    }

    @Test
    void findProfileByColleagueUuidShouldReturnExternalSystemException() {

        final var colleagueUuid = randomUUID();

        when(mockProfileDAO.get(any(UUID.class)))
                .thenReturn(profileAttributes(3));

        when(mockColleagueApiService.tryFindColleagueByUuid(any(UUID.class)))
                .thenThrow(ExternalSystemException.class);

        assertThatExceptionOfType(ExternalSystemException.class)
                .isThrownBy(() -> mockProfileService.findProfileByColleagueUuid(colleagueUuid))
                .withNoCause();

    }

    @Test
    void updateProfileAttributesShouldReturnUpdatedProfileAttributes() {

        when(mockProfileDAO.update(any(ProfileAttribute.class)))
                .thenReturn(1);

        var results = mockProfileService.updateProfileAttributes(profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).update(any(ProfileAttribute.class));

    }

    @Test
    void createProfileAttributesShouldReturnInsertedProfileAttributes() {

        when(mockProfileDAO.create(any(ProfileAttribute.class)))
                .thenReturn(1);

        var results = mockProfileService.createProfileAttributes(profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).create(any(ProfileAttribute.class));

    }

    @Test
    void createProfileAttributesWithNotUniqueName() {

        when(mockProfileDAO.create(any(ProfileAttribute.class)))
                .thenThrow(DuplicateKeyException.class);

        var exception = assertThrows(DatabaseConstraintViolationException.class,
                () -> mockProfileService.createProfileAttributes(profileAttributes(3)));

        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).create(any(ProfileAttribute.class));

    }


    @Test
    void deleteProfileAttributesShouldReturnDeletedProfileAttributes() {

        when(mockProfileDAO.delete(any(ProfileAttribute.class)))
                .thenReturn(1);

        var results = mockProfileService.deleteProfileAttributes(profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).delete(any(ProfileAttribute.class));

    }

}