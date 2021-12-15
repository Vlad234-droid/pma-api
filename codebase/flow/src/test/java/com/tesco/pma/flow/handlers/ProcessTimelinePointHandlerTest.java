package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.TimelinePointService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_DURATION;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;

@ActiveProfiles("test")
@SpringBootTest(classes = CamundaSpringBootTestConfig.class)
@ExtendWith(MockitoExtension.class)
public class ProcessTimelinePointHandlerTest {

    private static final String CODE = "code";
    private static final String PM_CYCLE_MIN_ONE = "1";
    private static final String PM_CYCLE_MAX_FIVE = "5";

    private final LocalDate startDate = LocalDate.of(2021, 1, 1);
    private final LocalDate endDate = LocalDate.of(2022, 1, 1);

    @MockBean
    private PMColleagueCycleService colleagueCycleService;

    @MockBean
    private TimelinePointService timelinePointService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @SpyBean
    private ProcessTimelinePointHandler handler;

    @Test
    void testEmptyColleagueCycles() throws Exception {

        var pmCycle = buildPmCycle();

        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();

        var pmElement = new PMElement();
        pmElement.setType(PMElementType.REVIEW);

        Mockito.doReturn(pmElement).when(handler).getParent(ec);
        Mockito.doReturn(Collections.emptyList()).when(colleagueCycleService)
                .getByCycleUuidWithoutTimelinePoint(pmCycle.getUuid());

        handler.execute(ec);

        Mockito.verifyNoInteractions(timelinePointService);
    }

    @Test
    void testCreateTimelinePoint() throws Exception {

        var pmCycle = buildPmCycle();

        var ec = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();

        var parentElement = new PMElement();
        parentElement.setType(PMElementType.REVIEW);
        parentElement.setCode(CODE);
        parentElement.setProperties(Map.of(PM_REVIEW_DURATION, "P1Y", PM_REVIEW_MIN, "1", PM_REVIEW_MAX, "5"));

        var ccUuid = UUID.randomUUID();
        var colleagueCycle = PMColleagueCycle.builder().uuid(ccUuid).build();

        Mockito.doReturn(parentElement).when(handler).getParent(ec);
        Mockito.doReturn(Collections.singletonList(colleagueCycle))
                .when(colleagueCycleService).getByCycleUuidWithoutTimelinePoint(pmCycle.getUuid());

        handler.execute(ec);

        Mockito.verify(timelinePointService).saveAll(Mockito.argThat((List<TimelinePoint> list) -> {
            var tp = list.iterator().next();
            var mapJson = tp.getProperties().getMapJson();
            return tp.getColleagueCycleUuid().equals(ccUuid)
                    && tp.getCode().equals(CODE)
                    && tp.getStartTime().equals(pmCycle.getStartTime())
                    && tp.getEndTime().equals(endDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                    && tp.getStatus().equals(PMTimelinePointStatus.STARTED)
                    && mapJson.containsKey(FlowParameters.START_DATE.name())
                    && mapJson.containsKey(FlowParameters.END_DATE.name())
                    && mapJson.get(PMReviewElement.PM_REVIEW_MIN).equals(PM_CYCLE_MIN_ONE)
                    && mapJson.get(PMReviewElement.PM_REVIEW_MAX).equals(PM_CYCLE_MAX_FIVE);
        }));
    }

    private PMCycle buildPmCycle() {
        return PMCycle.builder()
                .uuid(UUID.randomUUID())
                .type(PMCycleType.FISCAL)
                .startTime(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }


}
