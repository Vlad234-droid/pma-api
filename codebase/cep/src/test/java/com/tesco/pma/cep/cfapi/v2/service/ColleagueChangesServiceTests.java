package com.tesco.pma.cep.cfapi.v2.service;

import com.tesco.pma.cep.cfapi.v2.domain.EventType;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.tesco.pma.cep.cfapi.v2.service.TestDataUtils.COLLEAGUE_UUID;
import static com.tesco.pma.cep.cfapi.v2.service.TestDataUtils.buildAccount;
import static com.tesco.pma.cep.cfapi.v2.service.TestDataUtils.buildColleagueChangeEventPayload;
import static com.tesco.pma.cep.cfapi.v2.service.TestDataUtils.buildColleagueProfile;
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
    private ProfileService mockProfileService;
    @MockBean
    private UserManagementService mockUserManagementService;
    @MockBean
    private EventSender eventSender;

    @SpyBean
    private ColleagueChangesServiceImpl colleagueChangesService;

    @Test
    void processColleagueChangeEventWithJoinerEventType() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.JOINER);

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(eventSender).sendEvent(any(Event.class));
    }

    @Test
    void processColleagueChangeEventWithLeaverEventType() {

        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.LEAVER);

        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildColleagueProfile(COLLEAGUE_UUID));
        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildAccount(COLLEAGUE_UUID));

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1)).findAccountByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1))
                .changeAccountStatus(any(ChangeAccountStatusRequest.class));
    }

    @Test
    void processColleagueChangeEventWithModificationEventType() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.MODIFICATION);

        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildColleagueProfile(COLLEAGUE_UUID));
        when(mockProfileService.getColleagueFactsAPISupportedAttributes())
                .thenReturn(List.of("attribute1", "attribute2"));

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockProfileService, times(1)).getColleagueFactsAPISupportedAttributes();
        verify(eventSender, times(1))
                .sendEvent(Mockito.argThat(e -> e.getEventName().equals(EventNames.CEP_COLLEAGUE_UPDATED.name())
                        && e.getEventProperties().get(FlowParameters.COLLEAGUE.name()).equals(colleagueChangeEventPayload.getCurrent())
                        && e.getEventProperties().get(FlowParameters.COLLEAGUE_UUID.name()).equals(COLLEAGUE_UUID)));
    }

    @Test
    void processColleagueChangeEventWithDeletionEventType() {

        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.DELETION);

        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildColleagueProfile(COLLEAGUE_UUID));
        when(mockUserManagementService.findAccountByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildAccount(COLLEAGUE_UUID));

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1)).findAccountByColleagueUuid(COLLEAGUE_UUID);
        verify(mockUserManagementService, times(1))
                .changeAccountStatus(any(ChangeAccountStatusRequest.class));
    }

}