package com.tesco.pma.review.service;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.LocalTestConfig;
import com.tesco.pma.review.dao.OrgObjectiveDAO;
import com.tesco.pma.review.dao.ReviewAuditLogDAO;
import com.tesco.pma.review.dao.ReviewDAO;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import com.tesco.pma.review.domain.ReviewStatusCounter;
import com.tesco.pma.review.domain.TimelinePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final Integer NUMBER_1 = 1;

    private static final MapJson TIMELINE_POINT_PROPERTIES_INIT = new MapJson(
            Map.of(PM_REVIEW_MIN, "3",
                    PM_REVIEW_MAX, "5"
            ));

    final static String REVIEW_NOT_FOUND_MESSAGE =
            "Review not found for: {allowedStatuses=[DRAFT, DECLINED, APPROVED], number=1, operation=DELETE, tlPointUuid=ddb9ab0b-f50f-4442-8900-b03777ee0010}";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ReviewDAO mockReviewDAO;

    @MockBean
    private PMColleagueCycleService mockColleagueCycleService;

    @MockBean
    private TimelinePointDAO mockTimelinePointDAO;

    @MockBean
    private OrgObjectiveDAO mockOrgObjective;

    @MockBean
    private ReviewAuditLogDAO mockReviewAuditLogDAO;

    @MockBean
    private PMCycleService pmCycleService;

    @MockBean
    private ProfileService mockProfileService;

    @SpyBean
    private ReviewServiceImpl reviewService;

    @Test
    void getReviewByUuidShouldReturnReview() {
        final var randomUUID = UUID.randomUUID();
        final var expectedReview = Review.builder().build();
        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        when(mockReviewDAO.getByParams(any(), any(), any(), any()))
                .thenReturn(List.of(expectedReview));

        final var res = reviewService.getReview(
                randomUUID,
                randomUUID,
                OBJECTIVE,
                NUMBER_1);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void updateReviewShouldReturnUpdatedReview() {
        final var randomUUID = UUID.randomUUID();
        final var beforeReview = Review.builder().build();
        final var expectedReview = Review.builder()
                .type(OBJECTIVE)
                .status(DRAFT)
                .build();
        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();
        final var expectedReviewStats = ReviewStats.builder()
                .statusStats(Collections.emptyList())
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        when(mockReviewDAO.getReviewStats(any()))
                .thenReturn(expectedReviewStats);

        when(mockReviewDAO.update(any(), any()))
                .thenReturn(1);
        when(mockReviewDAO.getByParams(any(), any(), any(), any()))
                .thenReturn(List.of(beforeReview));

        final var res = reviewService.updateReview(expectedReview, randomUUID, randomUUID);

        assertThat(res)
                .returns(expectedReview.getProperties(), from(Review::getProperties));
    }

    @Test
    void createReviewShouldReturnCreatedReview() {
        final var randomUUID = UUID.randomUUID();
        final var expectedReview = Review.builder()
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .status(DRAFT)
                .build();
        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();
        final var expectedReviewStats = ReviewStats.builder()
                .statusStats(Collections.emptyList())
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        when(mockTimelinePointDAO.read(any()))
                .thenReturn(expectedTimelinePoint);

        when(mockReviewDAO.getReviewStats(any()))
                .thenReturn(expectedReviewStats);

        when(mockReviewDAO.create(any(Review.class)))
                .thenReturn(1);

        final var res = reviewService.createReview(expectedReview, randomUUID, randomUUID);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void deleteReviewNotExists() {
        final var tlPointUUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0010");
        final var colleagueUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
        final var performanceCycleUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0012");

        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .uuid(tlPointUUID)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .reviewType(OBJECTIVE)
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        final var reviewStats = ReviewStats.builder()
                .statusStats(List.of(new ReviewStatusCounter(DRAFT, 2, null)))
                .build();

        when(mockReviewDAO.getReviewStats(any()))
                .thenReturn(reviewStats);

        when(mockReviewDAO.deleteByParams(any(), any(), any(), any(), any()))
                .thenReturn(0);
        final var exception = assertThrows(NotFoundException.class,
                () -> reviewService.deleteReview(
                        performanceCycleUuid,
                        colleagueUuid,
                        OBJECTIVE,
                        1));

        assertEquals(REVIEW_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_NOT_FOUND_MESSAGE, exception.getMessage());

    }

    @Test
    void getReviewFilesByColleagueWithColleague() {
        // TODO Implement test

    }

    @Test
    void getReviewFilesByColleagueWithLineManager() {
        // TODO Implement test
    }

}
