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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void processColleagueChangeEventWithLeaverEventType() {

        var colleagueUuid = UUID.fromString("10000000-0000-0000-0000-000000000001");
        var feedId = "colleagues-jit-v1";

        var colleagueChangeEventPayload = new ColleagueChangeEventPayload();
        colleagueChangeEventPayload.setColleagueUuid(colleagueUuid);
        colleagueChangeEventPayload.setEventType(EventType.LEAVER);

        when(mockCepSubscribeProperties.getFeeds())
                .thenReturn(Map.of("jit", "colleagues-jit-v1"));
        when(mockProfileService.findProfileByColleagueUuid(colleagueUuid))
                .thenReturn(colleagueProfile(colleagueUuid));
        when(mockUserManagementService.findAccountByColleagueUuid(colleagueUuid))
                .thenReturn(account(colleagueUuid));

        colleagueChangesService.processColleagueChangeEvent(feedId, colleagueChangeEventPayload);

        verify(mockCepSubscribeProperties, times(1)).getFeeds();
        verify(mockProfileService, times(1)).findProfileByColleagueUuid(colleagueUuid);
        verify(mockUserManagementService, times(1)).findAccountByColleagueUuid(colleagueUuid);
        verify(mockUserManagementService, times(1))
                .changeAccountStatus(any(ChangeAccountStatusRequest.class));
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