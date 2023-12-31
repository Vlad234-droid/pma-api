package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class PMColleagueCycleHandlerTest {

    private static final String KEY = "l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1";
    public static final UUID UUID = java.util.UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    @MockBean
    private PMColleagueCycleService pmColleagueCycleService;
    @MockBean
    private ConfigEntryService configEntryService;
    @MockBean
    private PMCycleService pmCycleService;
    @SpyBean
    private PMColleagueCycleHandler handler;

    @Test
    void shouldSaveColleagueCycle() throws Exception {
        //given
        List<ColleagueEntity> colleagues = Collections.singletonList(new ColleagueEntity());
        DictionaryFilter<PMCycleStatus> statusFilter = DictionaryFilter.excludeFilter(PMCycleStatus.ACTIVE);
        Mockito.when(configEntryService.findColleaguesByCompositeKey(KEY, statusFilter)).thenReturn(colleagues);
        var pmCycle = buildPmCycle();
        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();
        var pmcr = new CompositePMCycleResponse();
        pmcr.setCycle(pmCycle);
        Mockito.when(pmCycleService.get(UUID, false)).thenReturn(pmcr);

        //when
        handler.execute(ec);

        //then
        Mockito.verify(pmColleagueCycleService, Mockito.times(1)).saveColleagueCycles(Mockito.anyCollection());
    }

    @Test
    void shouldSaveHiringColleagueCycle() throws Exception {
        //given
        var colleagueEntity = new ColleagueEntity();
        var hiringDate = LocalDate.now();
        colleagueEntity.setHireDate(hiringDate);
        List<ColleagueEntity> colleagues = Collections.singletonList(colleagueEntity);
        DictionaryFilter<PMCycleStatus> statusFilter = DictionaryFilter.excludeFilter(PMCycleStatus.ACTIVE);
        Mockito.when(configEntryService.findColleaguesByCompositeKey(KEY, statusFilter)).thenReturn(colleagues);
        var pmCycle = PMCycle.builder()
                .uuid(UUID)
                .type(PMCycleType.HIRING)
                .entryConfigKey(KEY)
                .status(PMCycleStatus.ACTIVE)
                .startTime(Instant.now())
                .build();
        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();
        var pmcr = new CompositePMCycleResponse();
        pmcr.setCycle(pmCycle);
        Mockito.when(pmCycleService.get(UUID, false)).thenReturn(pmcr);

        //when
        handler.execute(ec);

        //then
        Mockito.verify(pmColleagueCycleService, Mockito.times(1))
                .saveColleagueCycles(ArgumentMatchers.argThat((List<PMColleagueCycle> pmColleagueCycles) ->
                        pmColleagueCycles.get(0).getStartTime().equals(colleagueEntity.getHireDate().atStartOfDay()
                                .toInstant(ZoneOffset.UTC))));
    }

    private PMCycle buildPmCycle() {
        return PMCycle.builder()
                .uuid(UUID)
                .type(PMCycleType.FISCAL)
                .entryConfigKey(KEY)
                .status(PMCycleStatus.ACTIVE)
                .startTime(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }

}