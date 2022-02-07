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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class PMCycleRepeatHandlerTest {
    private static final String ENTRY_CONFIG_KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final UUID FILE_UUID = UUID.fromString("a0c0e913-5a45-4165-86a7-2fa9d5b1c8cb");
    private static final int PM_CYCLE_REPEATS_LEFT = 1;
    @SpyBean
    private PMCycleRepeatHandler handler;
    @MockBean
    private PMCycleService service;

    @Test
    void shouldStartCycle() throws Exception {
        //given
        var event = new EventSupport("PM_CYCLE_REPEAT");
        var compositeResponse = new CompositePMCycleResponse();
        var cycle = buildPmCycle();
        compositeResponse.setCycle(cycle);
        event.putProperty(FlowParameters.PM_CYCLE_UUID.name(), CYCLE_UUID);
        event.putProperty(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(), PM_CYCLE_REPEATS_LEFT);
        var executionContext = FlowTestUtil.executionBuilder()
                .withEvent(event)
                .build();
        Mockito.when(service.get(CYCLE_UUID, false)).thenReturn(compositeResponse);
        List<PMCycle> pmCycles = Mockito.mock(List.class);
        Mockito.when(pmCycles.size()).thenReturn(4);
        Mockito.when(service.getAll(Mockito.any(RequestQuery.class), Mockito.eq(false))).thenReturn(pmCycles);
        var nextCycle = buildPmCycle();
        nextCycle.setUuid(UUID.randomUUID());
        Mockito.when(service.create(cycle, cycle.getCreatedBy().getUuid())).thenReturn(nextCycle);
        nextCycle.setStatus(PMCycleStatus.REGISTERED);
        Mockito.when(service.updateStatus(nextCycle.getUuid(), PMCycleStatus.REGISTERED)).thenReturn(nextCycle);

        //when
        handler.execute(executionContext);

        //then
        Mockito.verify(service, Mockito.times(PM_CYCLE_REPEATS_LEFT)).start(Mockito.any(PMCycle.class));
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
                .startTime(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC))
                .endTime(LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .createdBy(colleagueSimple)
                .template(file)
                .metadata(metadata)
                .build();
    }

}