package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ColleagueDAO;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.colleague.profile.AbstractProfileTests;
import com.tesco.pma.colleague.profile.LocalTestConfig;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
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

import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest extends AbstractProfileTests {

    private static final String PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE =
            "Profile attribute name=emergencyContact for colleagueUuid=6d37262f-3a00-4706-a74b-6bf98be65765 already exists";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ConfigEntryDAO configEntryDAO;

    @MockBean
    private ColleagueDAO colleagueDAO;

    @MockBean
    private ProfileAttributeDAO mockProfileDAO;

    @MockBean
    private ColleagueApiService colleagueApiService;

    @SpyBean
    private ProfileServiceImpl profileService;

    private final UUID colleagueUuid = COLLEAGUE_UUID_1;

    @Test
    void findProfileByColleagueUuidShouldReturnProfile() {

        when(mockProfileDAO.get(any(UUID.class)))
                .thenReturn(profileAttributes(3));

        when(configEntryDAO.getColleague(any(UUID.class)))
                .thenReturn(randomColleague());

        Optional<ColleagueProfile> profileResponse = profileService.findProfileByColleagueUuid(colleagueUuid);
        assertThat(profileResponse).isPresent();
    }

    @Test
    void updateProfileAttributesShouldReturnUpdatedProfileAttributes() {

        when(mockProfileDAO.update(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.updateProfileAttributes(colleagueUuid, profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).update(any(TypedAttribute.class));

    }

    @Test
    void createProfileAttributesShouldReturnInsertedProfileAttributes() {

        when(mockProfileDAO.create(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.createProfileAttributes(colleagueUuid, profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).create(any(TypedAttribute.class));

    }

    @Test
    void createProfileAttributesWithNotUniqueName() {

        when(mockProfileDAO.create(any(TypedAttribute.class)))
                .thenThrow(DuplicateKeyException.class);

        var exception = assertThrows(DatabaseConstraintViolationException.class,
                () -> profileService.createProfileAttributes(colleagueUuid, profileAttributes(3)));

        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS.name(), exception.getCode());
        assertEquals(PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS_MESSAGE, exception.getMessage());

        verify(mockProfileDAO, times(1)).create(any(TypedAttribute.class));

    }


    @Test
    void deleteProfileAttributesShouldReturnDeletedProfileAttributes() {

        when(mockProfileDAO.delete(any(TypedAttribute.class)))
                .thenReturn(1);

        var results = profileService.deleteProfileAttributes(colleagueUuid, profileAttributes(3));

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(3);

        verify(mockProfileDAO, times(3)).delete(any(TypedAttribute.class));

    }

}