package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
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
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class CreateCycleHandlerTest {
    private static final String ENTRY_CONFIG_KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final Instant START_TIME = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant END_TIME = LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    @SpyBean
    private CreateCycleHandler handler;
    @MockBean
    private PMCycleService service;

    @Test
    void shouldCreateCycle() throws Exception {
        //given
        var cycle = buildPmCycle();
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, cycle)
                .build();
        Mockito.when(service.create(cycle, cycle.getCreatedBy().getUuid())).thenReturn(cycle);

        //when
        handler.execute(executionContext);

        //then
        Mockito.verify(service, Mockito.times(1)).create(cycle, cycle.getCreatedBy().getUuid());
    }

    private PMCycle buildPmCycle() {
        ColleagueSimple colleagueSimple = ColleagueSimple.builder()
                .uuid(COLLEAGUE_UUID)
                .build();
        return PMCycle.builder()
                .uuid(CYCLE_UUID)
                .type(PMCycleType.FISCAL)
                .entryConfigKey(ENTRY_CONFIG_KEY)
                .status(PMCycleStatus.COMPLETED)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .createdBy(colleagueSimple)
                .build();
    }

}