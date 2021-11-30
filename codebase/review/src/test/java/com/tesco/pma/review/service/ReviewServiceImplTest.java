package com.tesco.pma.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.LocalTestConfig;
import com.tesco.pma.review.dao.OrgObjectiveDAO;
import com.tesco.pma.review.dao.ReviewAuditLogDAO;
import com.tesco.pma.review.dao.ReviewDAO;
import com.tesco.pma.review.domain.PMCycleReviewTypeProperties;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import com.tesco.pma.review.domain.ReviewStatusCounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
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

    final static String REVIEW_NOT_FOUND_MESSAGE =
            "Review not found for: {allowedStatuses=[DRAFT, DECLINED, APPROVED], colleagueUuid=ddb9ab0b-f50f-4442-8900-b03777ee0011, number=1, operation=DELETE, performanceCycleUuid=ddb9ab0b-f50f-4442-8900-b03777ee0012, type=OBJECTIVE}";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ReviewDAO mockReviewDAO;

    @MockBean
    private OrgObjectiveDAO mockOrgObjective;

    @MockBean
    private ReviewAuditLogDAO mockReviewAuditLogDAO;

    @MockBean
    private PMCycleService pmCycleService;

    @SpyBean
    private ReviewServiceImpl reviewService;

//    @Test
//    void getReviewByUuidShouldReturnReview() {
//        final var randomUUID = UUID.randomUUID();
//        final var expectedReview = Review.builder().build();
//
//        when(mockReviewDAO.getReview(any(), any(), any(), any()))
//                .thenReturn(expectedReview);
//
//        final var res = reviewService.getReview(
//                randomUUID,
//                randomUUID,
//                OBJECTIVE,
//                NUMBER_1);
//
//        assertThat(res).isSameAs(expectedReview);
//    }

//    @Test
//    void updateReviewShouldReturnUpdatedReview() {
//        final var beforeReview = Review.builder().build();
//        final var expectedReview = Review.builder()
//                .type(OBJECTIVE)
//                .status(DRAFT)
//                .build();
//
//        when(mockReviewDAO.update(any(), any()))
//                .thenReturn(1);
//        when(mockReviewDAO.getReview(any(), any(), any(), any()))
//                .thenReturn(beforeReview);
//
//        final var res = reviewService.updateReview(expectedReview);
//
//        assertThat(res)
//                .returns(expectedReview.getProperties(), from(Review::getProperties));
//    }

    @Test
    void createReviewShouldReturnCreatedReview() {
        final var expectedReview = Review.builder()
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .status(DRAFT)
                .build();

        final var reviewTypeProperties = PMCycleReviewTypeProperties.builder()
                .min(3)
                .max(5)
                .build();

        when(mockReviewDAO.getPMCycleReviewTypeProperties(any(), any()))
                .thenReturn(reviewTypeProperties);

        when(mockReviewDAO.create(any(Review.class)))
                .thenReturn(1);

        final var res = reviewService.createReview(expectedReview);

        assertThat(res).isSameAs(expectedReview);
    }

//    @Test
//    void deleteReviewNotExists() {
//        final var colleagueUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
//        final var performanceCycleUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0012");
//
//        final var reviewTypeProperties = PMCycleReviewTypeProperties.builder()
//                .min(3)
//                .max(5)
//                .build();
//
//        when(mockReviewDAO.getPMCycleReviewTypeProperties(any(), any()))
//                .thenReturn(reviewTypeProperties);
//
//        final var reviewStats = ReviewStats.builder()
//                .statusStats(List.of(new ReviewStatusCounter(DRAFT, 2)))
//                .build();
//
//        when(mockReviewDAO.getReviewStats(any(), any(), any()))
//                .thenReturn(reviewStats);
//
//        when(mockReviewDAO.deleteReview(any(), any(), any(), any(), any()))
//                .thenReturn(0);
//        final var exception = assertThrows(NotFoundException.class,
//                () -> reviewService.deleteReview(
//                        performanceCycleUuid,
//                        colleagueUuid,
//                        OBJECTIVE,
//                        1));
//
//        assertEquals(REVIEW_NOT_FOUND.getCode(), exception.getCode());
//        assertEquals(REVIEW_NOT_FOUND_MESSAGE, exception.getMessage());
//
//    }
}
