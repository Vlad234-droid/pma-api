package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import org.camunda.bpm.engine.delegate.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

class ColleaguesEventsSendHandlerTest {

    private static final String EXPRESSION_VALUE = "injectedValue";
    private static final String IS_ERROR_SENSITIVE_EXPRESSION = "true";
    private static final String COMPOUND_KEY = "CompoundKey";

    @Mock
    private ConfigEntryService configEntryService;

    @Mock
    private EventSender eventSender;

    @Mock
    private NamedMessageSourceAccessor namedMessageSourceAccessor;

    private ColleaguesEventsSendHandler handler;
    private ExecutionContext executionContext;
    private PMCycle pmCycle;
    private final List<ColleagueEntity> colleagueEntities = new ArrayList<>();

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
        Expression expression = Mockito.mock(Expression.class);
        Mockito.when(expression.getExpressionText()).thenReturn(EXPRESSION_VALUE);
        Expression isErrorSensitiveExpression = Mockito.mock(Expression.class);
        Mockito.when(isErrorSensitiveExpression.getExpressionText()).thenReturn(IS_ERROR_SENSITIVE_EXPRESSION);
        handler = new ColleaguesEventsSendHandler(configEntryService, eventSender);
        handler.setEventNameExpression(expression);
        handler.setIsErrorSensitiveExpression(isErrorSensitiveExpression);
        executionContext = Mockito.mock(ExecutionContext.class);
        pmCycle = Mockito.mock(PMCycle.class);

    }

    @Test
    void executeTest() throws Exception {
        IntStream.range(1, 5).forEach(i -> colleagueEntities.add(createColleague()));
        Mockito.when(executionContext.getVariable(FlowParameters.PM_CYCLE)).thenReturn(pmCycle);
        Mockito.when(pmCycle.getEntryConfigKey()).thenReturn(COMPOUND_KEY);
        Mockito.when(configEntryService.findColleaguesByCompositeKey(COMPOUND_KEY)).thenReturn(colleagueEntities);

        handler.execute(executionContext);

        Mockito.verify(eventSender, Mockito.times(4))
                .sendEvent(Mockito.argThat(event -> EXPRESSION_VALUE.equals(event.getEventName())),
                        Mockito.isNull(),
                        Mockito.booleanThat(v -> v));
    }

    private ColleagueEntity createColleague() {
        var col = new ColleagueEntity();
        col.setUuid(UUID.randomUUID());
        return col;
    }

}
