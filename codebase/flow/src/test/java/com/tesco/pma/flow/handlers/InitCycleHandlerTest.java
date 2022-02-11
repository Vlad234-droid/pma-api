package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycle;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-08 21:55
 */
@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class InitCycleHandlerTest {
    private static final String COLLEAGUE_UUID = "bdb79be5-0000-0000-0000-deda4c47fa00";
    private static final String CYCLE_UUID = "00000000-0000-0000-0000-deda4c47fa01";
    private final LocalDate startDate = LocalDate.of(2021, 1, 1);
    private final LocalDate endDate = LocalDate.of(2022, 1, 1);
    @MockBean
    private PMCycleService cycleService;
    @MockBean
    private NamedMessageSourceAccessor messages;
    @SpyBean
    private InitCycleHandler handler;

    @Test
    void cycleColleague() throws Exception {
        var cycle = buildCycle();
        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, cycle)
                .withVariable(FlowParameters.COLLEAGUE_UUID, COLLEAGUE_UUID)
                .build();
        handler.execute(ec);
        verify(cycleService, Mockito.times(0)).get(Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    void cycleUuidColleague() throws Exception {
        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE_UUID, CYCLE_UUID)
                .withVariable(FlowParameters.COLLEAGUE_UUID, COLLEAGUE_UUID)
                .build();
        when(cycleService.get(UUID.fromString(CYCLE_UUID), false)).thenReturn(buildResponse(buildCycle()));
        handler.execute(ec);
        verify(cycleService, Mockito.times(1)).get(Mockito.any(), Mockito.anyBoolean());
    }

    private CompositePMCycleResponse buildResponse(PMCycle cycle) {
        var response = new CompositePMCycleResponse();
        response.setCycle(cycle);
        return response;
    }

    private PMCycle buildCycle() {
        return PMCycle.builder()
                .uuid(UUID.fromString(CYCLE_UUID))
                .type(PMCycleType.FISCAL)
                .startTime(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }
}