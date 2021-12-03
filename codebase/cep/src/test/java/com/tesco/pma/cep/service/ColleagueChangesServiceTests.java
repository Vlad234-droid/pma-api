package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.EventType;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ColleagueChangesServiceTests {

    @MockBean
    private CEPSubscribeProperties mockCepSubscribeProperties;
    @MockBean
    private ProfileService mockProfileService;
    @MockBean
    private UserManagementService mockUserManagementService;

    @SpyBean
    private ColleagueChangesServiceImpl colleagueChangesService;

    private final static UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @ParameterizedTest
    @CsvSource({
            "jit, colleagues-jit-v1",
            "immediate, colleagues-immediate-v1"
    })
    void processColleagueChangeEventWithJoinerEventType(String feedCode, String feedId) {
        processColleagueChangeEvent(feedCode, feedId, EventType.JOINER);
    }

    @ParameterizedTest
    @CsvSource({
            "jit, colleagues-jit-v1",
            "immediate, colleagues-immediate-v1"
    })
    void processColleagueChangeEventWithLeaverEventType(String feedCode, String feedId) {

        var colleagueChangeEventPayload = colleagueChangeEventPayload(EventType.LEAVER);

        when(mockCepSubscribeProperties.getFeeds())
                .thenReturn(Map.of(feedCode, feedId));
        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(colleagueProfile(COLLEAGUE_UUID));
        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(account(COLLEAGUE_UUID));

        colleagueChangesService.processColleagueChangeEvent(feedId, colleagueChangeEventPayload);

        verify(mockCepSubscribeProperties, times(1)).getFeeds();
        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1)).findAccountByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1))
                .changeAccountStatus(any(ChangeAccountStatusRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "jit, colleagues-jit-v1",
            "immediate, colleagues-immediate-v1"
    })
    void processColleagueChangeEventWithMoverEventType(String feedCode, String feedId) {
        processColleagueChangeEvent(feedCode, feedId, EventType.MOVER);
    }

    @ParameterizedTest
    @CsvSource({
            "jit, colleagues-jit-v1",
            "immediate, colleagues-immediate-v1"
    })
    void processColleagueChangeEventWithReinstatementEventType(String feedCode, String feedId) {
        processColleagueChangeEvent(feedCode, feedId, EventType.REINSTATEMENT);
    }

    private void processColleagueChangeEvent(String feedCode, String feedId, EventType eventType) {

        var colleagueChangeEventPayload = colleagueChangeEventPayload(eventType);

        when(mockCepSubscribeProperties.getFeeds())
                .thenReturn(Map.of(feedCode, feedId));
        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(colleagueProfile(COLLEAGUE_UUID));
        when(mockProfileService.updateColleague(COLLEAGUE_UUID, colleagueChangeEventPayload.getChangedAttributes()))
                .thenReturn(1);

        colleagueChangesService.processColleagueChangeEvent(feedId, colleagueChangeEventPayload);

        verify(mockCepSubscribeProperties, times(1)).getFeeds();
        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockProfileService, times(1)).updateColleague(COLLEAGUE_UUID,
                colleagueChangeEventPayload.getChangedAttributes());
    }

    private ColleagueChangeEventPayload colleagueChangeEventPayload(EventType eventType) {
        var colleagueChangeEventPayload = new ColleagueChangeEventPayload();
        colleagueChangeEventPayload.setColleagueUuid(COLLEAGUE_UUID);
        colleagueChangeEventPayload.setEventType(eventType);
        colleagueChangeEventPayload.setChangedAttributes(List.of("attribute1", "attribute2"));
        return colleagueChangeEventPayload;
    }

    private Optional<ColleagueProfile> colleagueProfile(UUID colleagueUuid) {
        var colleagueProfile = new ColleagueProfile();

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUuid);
        colleagueProfile.setColleague(colleague);
        colleagueProfile.setProfileAttributes(List.of());

        return Optional.of(colleagueProfile);
    }

    private Account account(UUID colleagueUuid) {
        var account = new Account();
        account.setId(colleagueUuid);
        account.setStatus(AccountStatus.ENABLED);
        return account;
    }

}