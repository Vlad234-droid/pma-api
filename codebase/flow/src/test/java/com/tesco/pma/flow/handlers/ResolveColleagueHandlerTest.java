package com.tesco.pma.flow.handlers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.cep.cfapi.v2.configuration.ColleagueChangesProperties;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.service.colleague.ColleagueApiService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cep.cfapi.v2.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResolveColleagueHandlerTest {

    private final ColleagueChangesProperties colleagueChangesProperties = mock(ColleagueChangesProperties.class);
    private final ColleagueApiService colleagueApiService = mock(ColleagueApiService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NamedMessageSourceAccessor messageSourceAccessor = mock(NamedMessageSourceAccessor.class);

    private final ResolveColleagueHandler handler = new ResolveColleagueHandler(colleagueChangesProperties,
            colleagueApiService, objectMapper, messageSourceAccessor);

    @Test
    void isForceTrueAndColleaguePresent() throws Exception {
        var colleagueUuid = UUID.randomUUID();
        var colleague = new Colleague();
        var executionContext = FlowTestUtil.executionBuilder()
                .withEvent(EventSupport.create("Test event", Map.of(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)))
                .build();
        when(colleagueChangesProperties.isForce()).thenReturn(true);
        when(colleagueApiService.findColleagueByUuid(colleagueUuid)).thenReturn(colleague);

        handler.execute(executionContext);

        assertEquals(colleague, executionContext.getVariable(FlowParameters.COLLEAGUE));
    }

    @Test
    void isForceTrueAndColleagueNotPresent() throws Exception {
        var colleagueUuid = UUID.randomUUID();
        var message = "Test msg";
        var executionContext = FlowTestUtil.executionBuilder()
                .withEvent(EventSupport.create("Test event", Map.of(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)))
                .build();
        when(colleagueChangesProperties.isForce()).thenReturn(true);
        when(colleagueApiService.findColleagueByUuid(colleagueUuid)).thenReturn(null);
        when(messageSourceAccessor.getMessage(COLLEAGUE_NOT_FOUND, Map.of("colleagueUuid", colleagueUuid))).thenReturn(message);


        var bpmnError = assertThrows(BpmnError.class, () -> handler.execute(executionContext));

        assertEquals(COLLEAGUE_NOT_FOUND.getCode(), bpmnError.getErrorCode());
        assertEquals(message, bpmnError.getMessage());
        verify(messageSourceAccessor).getMessage(COLLEAGUE_NOT_FOUND, Map.of("colleagueUuid", colleagueUuid));
    }

    @Test
    void isForceFalse() throws Exception {
        var executionContext = FlowTestUtil.executionBuilder()
                .withEvent(EventSupport.create("Event test name",
                        Map.of(FlowParameters.COLLEAGUE.name(), "{}")))
                .build();
        when(colleagueChangesProperties.isForce()).thenReturn(false);

        handler.execute(executionContext);

        assertNotNull(executionContext.getVariable(FlowParameters.COLLEAGUE, Colleague.class));
    }
}