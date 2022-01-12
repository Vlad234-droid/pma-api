package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class PMColleagueCycleHandlerTest {

    @MockBean
    private ConfigEntryService configEntryService;
    @MockBean
    private PMColleagueCycleService pmColleagueCycleService;
    @MockBean
    private PMCycleService pmCycleService;
    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;
    @SpyBean
    private PMColleagueCycleHandler handler;

    private static final String KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";

    @Test
    void shouldSaveColleagueCycle() throws Exception {
        //given
        var pmCycle = buildPmCycle();
        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();
        List<ColleagueEntity> colleagues = new ArrayList<>();
        colleagues.add(new ColleagueEntity());
        Mockito.when(pmColleagueCycleService.findColleagues(KEY, null, true)).thenReturn(colleagues);

        //when
        handler.execute(ec);

        //then
        Mockito.verify(pmColleagueCycleService, Mockito.times(1)).saveColleagueCycles(Mockito.anyCollection());
    }

    private PMCycle buildPmCycle() {
        return PMCycle.builder()
                .uuid(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .type(PMCycleType.FISCAL)
                .entryConfigKey(KEY)
                .status(PMCycleStatus.ACTIVE)
                .startTime(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }

}