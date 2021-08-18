package com.tesco.pma.profile.service;

import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.profile.AbstractTests;
import com.tesco.pma.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.profile.rest.model.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest extends AbstractTests {

    @Mock
    private ProfileAttributeDAO mockProfileDAO;

    @Mock
    private ColleagueApiService mockColleagueApiService;

    @InjectMocks
    private ProfileServiceImpl mockProfileService;

    @BeforeEach
    void setUp() {
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

        Optional<Profile> profile = mockProfileService.findProfileByColleagueUuid(colleagueUuid);
        assertThat(profile).isPresent();

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


}