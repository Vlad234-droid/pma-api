package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.LocalTestConfig;
import com.tesco.pma.objective.dao.ReviewDAO;
import com.tesco.pma.objective.dao.ReviewAuditLogDAO;
import com.tesco.pma.objective.domain.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.tesco.pma.objective.exception.ErrorCodes.REVIEW_NOT_FOUND_BY_UUID;
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

    final static String REVIEW_NOT_FOUND_MESSAGE =
            "Review not found for reviewUuid=ddb9ab0b-f50f-4442-8900-b03777ee0011";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ReviewDAO mockReviewDAO;

    @MockBean
    private ReviewAuditLogDAO mockReviewAuditLogDAO;

    private ReviewServiceImpl objectiveService;

    @BeforeEach
    void setUp() {
        objectiveService = new ReviewServiceImpl(mockReviewDAO, mockReviewAuditLogDAO, messages);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getReviewByUuidShouldReturnReview() {
        final var reviewUuid = UUID.randomUUID();
        final var expectedReview = Review.builder().build();

        when(mockReviewDAO.getReviewByUuid(any(UUID.class)))
                .thenReturn(expectedReview);

        final var res = objectiveService.getReviewByUuid(reviewUuid);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void updateReviewShouldReturnUpdatedReview() {
        final var beforeReview = Review.builder().build();
        final var expectedReview = Review.builder().build();

        when(mockReviewDAO.updateReview(any(Review.class)))
                .thenReturn(1);
        when(mockReviewDAO.getReview(any(), any(), any(), any()))
                .thenReturn(beforeReview);

        final var res = objectiveService.updateReview(expectedReview);

        assertThat(res)
                .returns(expectedReview.getProperties(), from(Review::getProperties));
    }

    @Test
    void createReviewShouldReturnCreatedReview() {
        final var expectedReview = Review.builder().build();

        when(mockReviewDAO.createReview(any(Review.class)))
                .thenReturn(1);

        final var res = objectiveService.createReview(expectedReview);

        assertThat(res).isSameAs(expectedReview);
    }

    @Test
    void deleteReviewNotExists() {
        final var reviewUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
        when(mockReviewDAO.deleteReview(any(UUID.class)))
                .thenReturn(0);
        final var exception = assertThrows(NotFoundException.class,
                () -> objectiveService.deleteReview(reviewUuid));

        assertEquals(REVIEW_NOT_FOUND_BY_UUID.getCode(), exception.getCode());
        assertEquals(REVIEW_NOT_FOUND_MESSAGE, exception.getMessage());

    }
}
