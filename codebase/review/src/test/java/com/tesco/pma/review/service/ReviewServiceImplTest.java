package com.tesco.pma.review.service;

import com.tesco.pma.api.ActionType;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.MessageSourceConfig;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.error.ApiExceptionHandler;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.dao.OrgObjectiveDAO;
import com.tesco.pma.review.dao.ReviewAuditLogDAO;
import com.tesco.pma.review.dao.ReviewDAO;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.ColleagueView;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import com.tesco.pma.review.domain.ReviewStatusCounter;
import com.tesco.pma.review.domain.TimelinePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND;
import static com.tesco.pma.review.util.TestDataUtils.buildOrgObjective;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = ReviewServiceImplTest.LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final Integer NUMBER_1 = 1;
    private static final String TEST = "Test";
    private static final String TEST_OLD = "Test old";

    private static final MapJson TIMELINE_POINT_PROPERTIES_INIT = new MapJson(
            Map.of(PM_REVIEW_MIN, "3",
                    PM_REVIEW_MAX, "5"
            ));

    private static final UUID COLLEAGUE_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID CURRENT_USER_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0014");

    static final String REVIEW_NOT_FOUND_MESSAGE = "Review not found for: {allowedStatuses=[DRAFT, DECLINED, APPROVED], "
            + "number=1, operation=DELETE, tlPointUuid=ddb9ab0b-f50f-4442-8900-b03777ee0010}";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ReviewDAO mockReviewDAO;

    @MockBean
    private PMColleagueCycleService mockColleagueCycleService;

    @MockBean
    private TimelinePointDAO mockTimelinePointDAO;

    @MockBean
    private OrgObjectiveDAO mockOrgObjectiveDAO;

    @MockBean
    private ReviewAuditLogDAO mockReviewAuditLogDAO;

    @SpyBean
    private ReviewServiceImpl reviewService;

    @MockBean
    private EventSender eventSender;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ReviewDecisionService mockReviewDecisionService;

    @MockBean
    private TimelinePointDecisionService mockTimelinePointDecisionService;

    @Profile("test")
    @Configuration
    @Import({MessageSourceAutoConfiguration.class,
            MessageSourceConfig.class,
            HttpMessageConvertersAutoConfiguration.class,
            ApiExceptionHandler.class,
            JacksonAutoConfiguration.class
    })
    static class LocalTestConfig {
    }

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

        when(mockReviewDAO.read(any()))
                .thenReturn(expectedReview);

        final var res = reviewService.getReview(randomUUID);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void getReviewByBusinessKeysShouldReturnReview() {
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
                OBJECTIVE,
                NUMBER_1);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void getReviewsByBusinessKeysShouldReturnReviews() {
        final var randomUUID = UUID.randomUUID();
        final var expectedReview = Review.builder().build();
        final var reviews = List.of(expectedReview);
        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        when(mockReviewDAO.getByParams(any(), any(), any(), any()))
                .thenReturn(reviews);

        final var res = reviewService.getReviews(
                randomUUID,
                OBJECTIVE);

        assertThat(res).isSameAs(reviews);
    }

    @Test
    void getReviewsByBusinessKeysAndStatusShouldReturnReviews() {
        final var randomUUID = UUID.randomUUID();
        final var expectedReview = Review.builder().build();
        final var reviews = List.of(expectedReview);
        final var expectedColleagueCycle = PMColleagueCycle.builder().build();
        final var expectedTimelinePoint = TimelinePoint.builder()
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();

        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(expectedColleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(expectedTimelinePoint));

        when(mockReviewDAO.getByParams(any(), any(), any(), any()))
                .thenReturn(reviews);

        final var res = reviewService.getReviews(
                randomUUID,
                OBJECTIVE,
                DRAFT);

        assertThat(res).isSameAs(reviews);
    }

    @Test
    void getReviewsByColleagueShouldReturnReviews() {
        final var randomUUID = UUID.randomUUID();
        final var expectedReview = Review.builder().build();
        final var reviews = List.of(expectedReview);

        when(mockReviewDAO.getReviewsByColleague(any(), any()))
                .thenReturn(reviews);

        final var res = reviewService.getReviewsByColleague(
                randomUUID);

        assertThat(res).isSameAs(reviews);
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
                .status(DRAFT)
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

        when(mockReviewDecisionService.getReviewAllowedStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));
        when(mockReviewDecisionService.getReviewAllowedPrevStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));

        final var res = reviewService.updateReview(expectedReview, randomUUID, null);

        assertThat(res)
                .returns(expectedReview.getProperties(), from(Review::getProperties));
    }

    @Test
    void updateReviewsStatus() {
        //given
        final var randomUUID = UUID.randomUUID();
        final var review = Review.builder()
                .type(OBJECTIVE)
                .status(DRAFT)
                .number(1)
                .build();
        final var reviews = List.of(review);
        final var timelinePoint = TimelinePoint.builder()
                .status(DRAFT)
                .uuid(randomUUID)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();
        final var colleagueCycle = PMColleagueCycle.builder().build();
        final var expectedReviewStats = ReviewStats.builder()
                .statusStats(Collections.emptyList())
                .build();
        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(colleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(timelinePoint));
        when(mockReviewDecisionService.getReviewAllowedStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));
        when(mockReviewDecisionService.getReviewAllowedPrevStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));
        when(mockReviewDAO.getReviewStats(any()))
                .thenReturn(expectedReviewStats);
        when(mockReviewDAO.updateStatusByParams(any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(mockReviewDAO.getByParams(any(), any(), any(), any()))
                .thenReturn(reviews);

        //when
        reviewService.updateReviewsStatus(randomUUID, OBJECTIVE, reviews, WAITING_FOR_APPROVAL, null, randomUUID);

        //then
        verify(mockReviewDAO, times(1)).getByParams(timelinePoint.getUuid(),
                review.getType(),
                null,
                1);
        verify(mockReviewDAO, times(1)).updateStatusByParams(timelinePoint.getUuid(),
                OBJECTIVE,
                review.getNumber(),
                WAITING_FOR_APPROVAL,
                List.of(DRAFT));
    }

    @Test
    void updateReviews() {
        //given
        final var randomUUID = UUID.randomUUID();
        final var review = Review.builder()
                .type(OBJECTIVE)
                .status(DRAFT)
                .build();
        final var reviews = List.of(review);
        final var timelinePoint = TimelinePoint.builder()
                .status(DRAFT)
                .uuid(randomUUID)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .build();
        final var colleagueCycle = PMColleagueCycle.builder().build();
        final var expectedReviewStats = ReviewStats.builder()
                .statusStats(Collections.emptyList())
                .build();
        when(mockColleagueCycleService.getByCycleUuid(any(), any(), any()))
                .thenReturn(List.of(colleagueCycle));

        when(mockTimelinePointDAO.getByParams(any(), any(), any()))
                .thenReturn(List.of(timelinePoint));
        when(mockReviewDecisionService.getReviewAllowedStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));
        when(mockReviewDAO.getReviewStats(any()))
                .thenReturn(expectedReviewStats);

        //when
        reviewService.updateReviews(randomUUID, OBJECTIVE, reviews, randomUUID);

        //then
        verify(mockReviewDAO, times(1)).getByParams(timelinePoint.getUuid(),
                review.getType(),
                null,
                1);
        verify(mockReviewDAO, times(1)).create(review);
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

        when(mockReviewDecisionService.getReviewAllowedStatuses(any(), any()))
                .thenReturn(List.of(DRAFT));

        final var res = reviewService.createReview(expectedReview, randomUUID);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void deleteReviewNotExists() {
        final var tlPointUUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0010");

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

        when(mockReviewDecisionService.getReviewAllowedStatuses(any(), any()))
                .thenReturn(List.of(DRAFT, DECLINED, APPROVED));
        final var exception = assertThrows(NotFoundException.class,
                () -> reviewService.deleteReview(
                        COLLEAGUE_UUID,
                        OBJECTIVE,
                        1));

        assertEquals(REVIEW_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_NOT_FOUND_MESSAGE, exception.getMessage());

    }

    @Test
    void getCycleTimelineByColleagueShouldReturnTimelinePoints() {
        final var randomUUID = UUID.randomUUID();
        final var tlPointUUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0010");
        final var expectedTimelinePoint = TimelinePoint.builder()
                .uuid(tlPointUUID)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .reviewType(OBJECTIVE)
                .build();
        final var expectedTimelinePoints = List.of(expectedTimelinePoint);

        when(mockTimelinePointDAO.getTimeline(any(), any()))
                .thenReturn(expectedTimelinePoints);

        final var res = reviewService.getCycleTimelineByColleague(randomUUID);

        assertThat(res).isSameAs(expectedTimelinePoints);
    }

    @Test
    void getTeamViewShouldReturnColleagueViews() {
        final var randomUUID = UUID.randomUUID();
        final var tlPointUUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0010");
        final var expectedTimelinePoint = TimelinePoint.builder()
                .uuid(tlPointUUID)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .reviewType(OBJECTIVE)
                .build();
        final var expectedTimelinePoints = List.of(expectedTimelinePoint);
        final var colleagueView = ColleagueView.builder()
                .timeline(expectedTimelinePoints)
                .build();
        final var expectedColleagueViews = List.of(colleagueView);

        when(mockReviewDAO.getTeamView(any(), any()))
                .thenReturn(expectedColleagueViews);

        final var res = reviewService.getTeamView(randomUUID, 1);

        assertThat(res).isSameAs(expectedColleagueViews);
    }

    @Test
    void createOrgObjectivesShouldNotCreateNewVersion() {
        final var randomUUID = UUID.randomUUID();
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        when(mockOrgObjectiveDAO.getAll())
                .thenReturn(orgObjectives);

        reviewService.createOrgObjectives(orgObjectives, randomUUID);

        verify(mockOrgObjectiveDAO, times(0)).getMaxVersion();
        verify(mockOrgObjectiveDAO, times(0)).create(orgObjective);
    }

    @Test
    void createOrgObjectives() {
        final var randomUUID = UUID.randomUUID();
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        final var oldOrgObjective = buildOrgObjective(TEST_OLD);
        final var oldOrgObjectives = List.of(oldOrgObjective);
        when(mockOrgObjectiveDAO.getAll())
                .thenReturn(oldOrgObjectives);

        reviewService.createOrgObjectives(orgObjectives, randomUUID);

        verify(mockOrgObjectiveDAO, times(1)).getMaxVersion();
        verify(mockOrgObjectiveDAO, times(1)).create(orgObjective);
    }

    @Test
    void getAllOrgObjectives() {
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        when(mockOrgObjectiveDAO.getAll())
                .thenReturn(orgObjectives);

        var res = reviewService.getAllOrgObjectives();

        assertThat(res).isSameAs(orgObjectives);
    }

    @Test
    void publishOrgObjectives() {
        final var randomUUID = UUID.randomUUID();
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        when(mockOrgObjectiveDAO.getAllPublished())
                .thenReturn(orgObjectives);
        when(mockOrgObjectiveDAO.unpublish())
                .thenReturn(1);
        when(mockOrgObjectiveDAO.publish())
                .thenReturn(1);

        var res = reviewService.publishOrgObjectives(randomUUID);

        assertThat(res).isSameAs(orgObjectives);
    }

    @Test
    void createAndPublishOrgObjectives() {
        final var randomUUID = UUID.randomUUID();
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        final var oldOrgObjective = buildOrgObjective(TEST_OLD);
        final var oldOrgObjectives = List.of(oldOrgObjective);
        when(mockOrgObjectiveDAO.getAll())
                .thenReturn(oldOrgObjectives);
        when(mockOrgObjectiveDAO.getAllPublished())
                .thenReturn(orgObjectives);
        when(mockOrgObjectiveDAO.unpublish())
                .thenReturn(1);
        when(mockOrgObjectiveDAO.publish())
                .thenReturn(1);

        var res = reviewService.createAndPublishOrgObjectives(orgObjectives, randomUUID);

        verify(mockOrgObjectiveDAO, times(1)).getMaxVersion();
        verify(mockOrgObjectiveDAO, times(1)).create(orgObjective);

        assertThat(res).isSameAs(orgObjectives);
    }

    @Test
    void getPublishedOrgObjectives() {
        final var orgObjective = buildOrgObjective(TEST);
        final var orgObjectives = List.of(orgObjective);
        when(mockOrgObjectiveDAO.getAllPublished())
                .thenReturn(orgObjectives);

        var res = reviewService.getPublishedOrgObjectives();

        assertThat(res).isSameAs(orgObjectives);
    }

    @Test
    void getAuditOrgObjectiveReport() {
        final var requestQuery = new RequestQuery();
        final var auditOrgObjectiveReport = AuditOrgObjectiveReport.builder()
                .action(ActionType.PUBLISH)
                .build();
        final var auditOrgObjectiveReports = List.of(auditOrgObjectiveReport);
        when(mockReviewAuditLogDAO.getAuditOrgObjectiveReport(requestQuery))
                .thenReturn(auditOrgObjectiveReports);

        var res = reviewService.getAuditOrgObjectiveReport(requestQuery);

        assertThat(res).isSameAs(auditOrgObjectiveReports);
    }
}
