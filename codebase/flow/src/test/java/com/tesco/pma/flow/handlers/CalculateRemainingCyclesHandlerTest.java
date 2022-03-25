package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.flow.FlowParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class CalculateRemainingCyclesHandlerTest {
    private static final String ENTRY_CONFIG_KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final Instant START_TIME = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant END_TIME = LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    @SpyBean
    private CalculateRemainingCyclesHandler handler;

    @Test
    void shouldCalculateFromMetadataProperty() throws Exception {
        //given
        var cycle = buildPmCycle();
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, cycle)
                .build();

        //when
        handler.execute(executionContext);
        var result = executionContext.getVariable(FlowParameters.PM_CYCLE_REPEATS_LEFT, Integer.class);

        //then
        assertEquals(4, result);
    }

    @Test
    void shouldCalculateFromMapProperty() throws Exception {
        //given
        var cycle = buildPmCycle();
        cycle.setProperties(new MapJson());
        cycle.getProperties().setMapJson(new HashMap<>());
        cycle.getProperties().getMapJson().put(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), "4");
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, cycle)
                .build();

        //when
        handler.execute(executionContext);
        var result = executionContext.getVariable(FlowParameters.PM_CYCLE_REPEATS_LEFT, Integer.class);

        //then
        assertEquals(3, result);
    }

    private PMCycle buildPmCycle() {
        ColleagueSimple colleagueSimple = ColleagueSimple.builder()
                .uuid(COLLEAGUE_UUID)
                .build();
        var metadata = new PMCycleMetadata();
        var element = new PMCycleElement();
        var properties = new HashMap<String, String>();
        properties.put(PMCycleElement.PM_CYCLE_MAX, "5");
        element.setProperties(properties);
        metadata.setCycle(element);
        return PMCycle.builder()
                .uuid(CYCLE_UUID)
                .type(PMCycleType.FISCAL)
                .entryConfigKey(ENTRY_CONFIG_KEY)
                .status(PMCycleStatus.COMPLETED)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .createdBy(colleagueSimple)
                .metadata(metadata)
                .build();
    }

}