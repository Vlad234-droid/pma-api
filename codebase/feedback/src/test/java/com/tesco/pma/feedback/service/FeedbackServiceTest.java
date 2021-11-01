package com.tesco.pma.feedback.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.dao.FeedbackDAO;
import com.tesco.pma.feedback.service.impl.FeedbackServiceImpl;
import com.tesco.pma.feedback.util.TestDataUtil;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FeedbackServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Autowired
    private FeedbackService underTest;

    @MockBean
    private FeedbackDAO feedbackDAO;

    @MockBean
    private FeedbackItemService feedbackItemService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Test
    void create() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();

        //when
        Feedback result = underTest.create(feedback);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void createShouldThrowDatabaseConstraintViolationException() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        when(feedbackDAO.insert(feedback)).thenThrow(DuplicateKeyException.class);

        //then
        assertThrows(DatabaseConstraintViolationException.class, () -> underTest.create(feedback));
    }

    @Test
    void markAsRead() {
        //when
        underTest.markAsRead(TestDataUtil.FEEDBACK_UUID_UNREAD);

        //then
        verify(feedbackDAO, times(1)).markAsRead(TestDataUtil.FEEDBACK_UUID_UNREAD);
    }

    @Test
    void findAll() {
        //given
        RequestQuery requestQuery = new RequestQuery();
        Feedback feedback1 = TestDataUtil.buildFeedback();
        feedback1.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        Feedback feedback2 = TestDataUtil.buildFeedback();
        feedback2.setUuid(TestDataUtil.FEEDBACK_UUID_UNREAD);
        List<Feedback> feedbacks = List.of(feedback1, feedback2);
        when(feedbackDAO.findAll(requestQuery)).thenReturn(feedbacks);

        //when
        List<Feedback> result = underTest.findAll(requestQuery);

        //then
        assertThat(result)
                .hasSize(2)
                .element(0)
                .isSameAs(feedback1);
    }

    @Test
    void findOne() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        when(feedbackDAO.getByUuid(TestDataUtil.FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when
        Feedback result = underTest.findOne(TestDataUtil.FEEDBACK_UUID_LAST);

        //then
        verify(feedbackDAO, times(1)).getByUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        assertEquals(result.getColleagueUuid(), TestDataUtil.COLLEAGUE_UUID);
    }

    @Test
    void update() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        feedback.setStatus(FeedbackStatus.COMPLETED);
        when(feedbackDAO.update(eq(feedback), any())).thenReturn(1);

        //when
        Feedback result = underTest.update(feedback);

        //then
        assertEquals(result.getStatus(), FeedbackStatus.COMPLETED);
    }
}