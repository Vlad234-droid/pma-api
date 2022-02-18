package com.tesco.pma.cep.v2.service;

import com.tesco.pma.cep.v2.configuration.ColleagueFactsApiProperties;
import com.tesco.pma.cep.v2.domain.EventType;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.service.EventSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.tesco.pma.cep.v2.service.TestDataUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ColleagueChangesServiceTests {

    @MockBean
    private ColleagueFactsApiProperties mockColleagueFactsApiProperties;
    @MockBean
    private ProfileService mockProfileService;
    @MockBean
    private UserManagementService mockUserManagementService;
    @MockBean
    private EventSender eventSender;

    @SpyBean
    private ColleagueChangesServiceImpl colleagueChangesService;

    @Test
    void processColleagueChangeEventWithJoinerEventTypeAndForceMode() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.JOINER);

        when(mockColleagueFactsApiProperties.isForce())
                .thenReturn(true);
        when(mockProfileService.create(COLLEAGUE_UUID))
                .thenReturn(1);

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockColleagueFactsApiProperties, times(1)).isForce();
        verify(mockProfileService, times(1)).create(COLLEAGUE_UUID);
        verify(eventSender, times(2)).sendEvent(any(Event.class));
    }

    @Test
    void processColleagueChangeEventWithJoinerEventType() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.JOINER);

        when(mockColleagueFactsApiProperties.isForce())
                .thenReturn(false);
        when(mockProfileService.create(any(Colleague.class)))
                .thenReturn(1);

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockColleagueFactsApiProperties, times(1)).isForce();
        verify(mockProfileService, times(1)).create(any(Colleague.class));
        verify(eventSender, times(2)).sendEvent(any(Event.class));
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
    void processColleagueChangeEventWithModificationEventTypeAndForceMode() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.MODIFICATION);

        when(mockColleagueFactsApiProperties.isForce())
                .thenReturn(true);
        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildColleagueProfile(COLLEAGUE_UUID));
        when(mockProfileService.getColleagueFactsAPISupportedAttributes())
                .thenReturn(List.of("attribute1", "attribute2"));
        when(mockProfileService.updateColleague(COLLEAGUE_UUID, colleagueChangeEventPayload.getChangedAttributes()))
                .thenReturn(1);

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockColleagueFactsApiProperties, times(1)).isForce();
        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockProfileService, times(1)).getColleagueFactsAPISupportedAttributes();
        verify(mockProfileService, times(1)).updateColleague(COLLEAGUE_UUID,
                colleagueChangeEventPayload.getChangedAttributes());
    }

    @Test
    void processColleagueChangeEventWithModificationEventType() {
        var colleagueChangeEventPayload = buildColleagueChangeEventPayload(EventType.MODIFICATION);

        when(mockColleagueFactsApiProperties.isForce())
                .thenReturn(false);
        when(mockProfileService.findProfileByColleagueUuid(COLLEAGUE_UUID))
                .thenReturn(buildColleagueProfile(COLLEAGUE_UUID));
        when(mockProfileService.getColleagueFactsAPISupportedAttributes())
                .thenReturn(List.of("attribute1", "attribute2"));
        when(mockProfileService.updateColleague(any(Colleague.class)))
                .thenReturn(1);

        colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);

        verify(mockColleagueFactsApiProperties, times(1)).isForce();
        verify(mockProfileService, times(1)).findProfileByColleagueUuid(COLLEAGUE_UUID);
        verify(mockProfileService, times(1)).getColleagueFactsAPISupportedAttributes();
        verify(mockProfileService, times(1)).updateColleague(any(Colleague.class));
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