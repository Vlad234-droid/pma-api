package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.file.api.File;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class BuildNextCycleHandlerTest {
    private static final String ENTRY_CONFIG_KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final UUID FILE_UUID = UUID.fromString("a0c0e913-5a45-4165-86a7-2fa9d5b1c8cb");
    private static final int PM_CYCLE_REPEATS_LEFT = 1;
    private static final Instant START_TIME = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant END_TIME = LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    @SpyBean
    private BuildNextCycleHandler handler;
    @MockBean
    private PMCycleService service;

    @Test
    void shouldBuildNextCycle() throws Exception {
        //given
        var event = new EventSupport("PM_CYCLE_REPEAT");
        var compositeResponse = new CompositePMCycleResponse();
        var cycle = buildPmCycle();
        compositeResponse.setCycle(cycle);
        event.putProperty(FlowParameters.PM_CYCLE_UUID.name(), CYCLE_UUID);
        event.putProperty(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), PM_CYCLE_REPEATS_LEFT);
        var executionContext = FlowTestUtil.executionBuilder()
                .withEvent(event)
                .withVariable(FlowParameters.PM_CYCLE, cycle)
                .build();
        Mockito.when(service.get(CYCLE_UUID, false)).thenReturn(compositeResponse);
        List<PMCycle> pmCycles = Mockito.mock(List.class);
        Mockito.when(pmCycles.size()).thenReturn(4);
        Mockito.when(service.findAll(Mockito.any(RequestQuery.class), Mockito.eq(false))).thenReturn(pmCycles);

        //when
        handler.execute(executionContext);
        var result = executionContext.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);

        //then
        assertNull(result.getUuid());
        assertNotNull(result.getProperties());
        assertNotEquals(START_TIME, result.getStartTime());
    }

    private PMCycle buildPmCycle() {
        ColleagueSimple colleagueSimple = ColleagueSimple.builder()
                .uuid(COLLEAGUE_UUID)
                .build();
        var file = new File();
        file.setUuid(FILE_UUID);
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
                .template(file)
                .metadata(metadata)
                .build();
    }

}