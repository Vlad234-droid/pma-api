package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.EventSender;
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

public class ColleagueEventsSendHandlerTest {

    private static final String EXPRESSION_VALUE = "injectedValue";
    private static final String COMPOUND_KEY = "CompoundKey";

    @Mock
    private ConfigEntryService configEntryService;

    @Mock
    private EventSender eventSender;

    private ColleagueEventsSendHandler handler;
    private ExecutionContext executionContext;
    private PMCycle pmCycle;
    private final List<ColleagueEntity> colleagueEntities = new ArrayList<>();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        Expression expression = Mockito.mock(Expression.class);
        Mockito.when(expression.getExpressionText()).thenReturn(EXPRESSION_VALUE);
        handler = new ColleagueEventsSendHandler(configEntryService, eventSender);
        handler.setInjectedValue(expression);
        executionContext = Mockito.mock(ExecutionContext.class);
        pmCycle = Mockito.mock(PMCycle.class);

    }

    @Test
    public void executeTest() throws Exception {
        IntStream.range(1, 5).forEach(i -> colleagueEntities.add(createColleague()));
        Mockito.when(executionContext.getVariable(Mockito.eq(FlowParameters.PM_CYCLE))).thenReturn(pmCycle);
        Mockito.when(pmCycle.getEntryConfigKey()).thenReturn(COMPOUND_KEY);
        Mockito.when(configEntryService.findColleaguesByCompositeKey(Mockito.eq(COMPOUND_KEY))).thenReturn(colleagueEntities);

        handler.execute(executionContext);

        Mockito.verify(eventSender, Mockito.times(4))
                .send(Mockito.argThat(event -> EXPRESSION_VALUE.equals(event.getEventName())));
    }

    private ColleagueEntity createColleague() {
        var col = new ColleagueEntity();
        col.setUuid(UUID.randomUUID());
        return col;
    }

}
