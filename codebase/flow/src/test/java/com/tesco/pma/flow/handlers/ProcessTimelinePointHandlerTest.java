package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.camunda.flow.CamundaHandlerTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.TimelinePointService;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaHandlerTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProcessTimelinePointHandlerTest {

    private static final String CODE = "code";
    private static final String PM_CYCLE_MIN_ONE = "1";
    private static final String PM_CYCLE_MAX_FIVE = "5";
    private static final FixedValue STATUS_VALUES = new FixedValue("REGISTERED,ACTIVE");

    private final LocalDate startDate = LocalDate.of(2021, 1, 1);
    private final LocalDate endDate = LocalDate.of(2022, 1, 1);

    @MockBean
    private PMColleagueCycleService colleagueCycleService;

    @MockBean
    private TimelinePointService timelinePointService;

    @SpyBean
    private ProcessTimelinePointHandler handler;

    @Test
    void testEmptyColleagueCycles() throws Exception {

        var pmCycle = buildPMCycle();
        var parentElement = new PMElement();
        parentElement.setType(PMElementType.REVIEW);

        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.MODEL_PARENT_ELEMENT, parentElement)
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .withVariable(FlowParameters.START_DATE, startDate)
                .build();

        Mockito.doReturn(Collections.emptyList()).when(colleagueCycleService)
                .getByCycleUuidWithoutTimelinePoint(pmCycle.getUuid(), null,
                        DictionaryFilter.includeFilter(PMCycleStatus.REGISTERED, PMCycleStatus.ACTIVE));

        handler.setOldStatusValues(STATUS_VALUES);
        handler.execute(ec);

        Mockito.verifyNoInteractions(timelinePointService);
    }

    @Test
    void testCreateTimelinePoint() throws Exception {

        var pmCycle = buildPMCycle();

        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .withVariable(FlowParameters.START_DATE, startDate)
                .withVariable(FlowParameters.START_DATE_S, HandlerUtils.formatDate(startDate))
                .withVariable(FlowParameters.END_DATE, endDate)
                .withVariable(FlowParameters.END_DATE_S, HandlerUtils.formatDate(endDate))
                .withVariable(PM_REVIEW_MIN, PM_CYCLE_MIN_ONE)
                .withVariable(PM_REVIEW_MAX, PM_CYCLE_MAX_FIVE)
                .build();

        var ccUuid = UUID.randomUUID();
        var colleagueCycle = PMColleagueCycle.builder()
                .uuid(ccUuid)
                .startTime(pmCycle.getStartTime())
                .endTime(pmCycle.getEndTime())
                .build();

        Mockito.doReturn(Collections.singletonList(colleagueCycle))
                .when(colleagueCycleService).getByCycleUuidWithoutTimelinePoint(pmCycle.getUuid(),
                null, DictionaryFilter.includeFilter(PMCycleStatus.REGISTERED, PMCycleStatus.ACTIVE));

        handler.setOldStatusValues(STATUS_VALUES);
        handler.execute(ec);

        ArgumentCaptor<List<TimelinePoint>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(timelinePointService).saveAll(argumentCaptor.capture());

        List<TimelinePoint> capturedArgument = argumentCaptor.getValue();
        assertFalse(capturedArgument.isEmpty());
        var tp = capturedArgument.get(0);
        assertTimelinePoint(colleagueCycle, tp);
    }

    private void assertTimelinePoint(PMColleagueCycle ccCycle, TimelinePoint tp) {
        assertNotNull(tp.getUuid());
        assertEquals(ccCycle.getUuid(), tp.getColleagueCycleUuid());
        //todo check code
//        assertEquals(CODE, tp.getCode());
        assertEquals(ccCycle.getStartTime(), tp.getStartTime());
        assertEquals(endDate.atStartOfDay().toInstant(ZoneOffset.UTC), tp.getEndTime());
        assertEquals(PMTimelinePointStatus.STARTED, tp.getStatus());

        var mapJson = tp.getProperties().getMapJson();
        assertTrue(mapJson.containsKey(FlowParameters.START_DATE.name()));
        assertTrue(mapJson.containsKey(FlowParameters.END_DATE.name()));
        assertEquals(PM_CYCLE_MIN_ONE, mapJson.get(PMReviewElement.PM_REVIEW_MIN));
        assertEquals(PM_CYCLE_MAX_FIVE, mapJson.get(PMReviewElement.PM_REVIEW_MAX));
    }

    private PMCycle buildPMCycle() {
        return PMCycle.builder()
                .uuid(UUID.randomUUID())
                .type(PMCycleType.FISCAL)
                .startTime(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }
}
