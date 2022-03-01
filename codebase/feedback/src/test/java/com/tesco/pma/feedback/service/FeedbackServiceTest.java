package com.tesco.pma.feedback.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.dao.FeedbackDAO;
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

import static com.tesco.pma.feedback.util.TestDataUtil.COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACKS_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FeedbackServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Autowired
    private FeedbackService underTest;

    @MockBean
    private FeedbackDAO feedbackDAO;

    @MockBean
    private EventSender eventSender;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Test
    void createFeedback() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();

        //when
        Feedback result = underTest.create(feedback);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void shouldThrowDatabaseConstraintViolationExceptionWhenCreateFeedbackWithDuplicateKey() {
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
    void findAllFeedbacks() {
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
        assertEquals(2, result.size());
        assertEquals(feedback1, result.get(0));
    }

    @Test
    void findOneFeedback() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        when(feedbackDAO.getByUuid(TestDataUtil.FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when
        Feedback result = underTest.findOne(TestDataUtil.FEEDBACK_UUID_LAST);

        //then
        verify(feedbackDAO, times(1)).getByUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        assertEquals(COLLEAGUE_UUID, result.getColleagueUuid());
    }

    @Test
    void findGivenFeedbackCount() {
        //given
        when(feedbackDAO.findGivenFeedbackCount(COLLEAGUE_UUID)).thenReturn(FEEDBACKS_COUNT);

        //when
        var result = underTest.findGivenFeedbackCount(COLLEAGUE_UUID);

        //then
        verify(feedbackDAO, times(1)).findGivenFeedbackCount(COLLEAGUE_UUID);
        assertEquals(FEEDBACKS_COUNT, result);
    }

    @Test
    void findRequestedFeedbackCount() {
        //given
        when(feedbackDAO.findRequestedFeedbackCount(COLLEAGUE_UUID)).thenReturn(FEEDBACKS_COUNT);

        //when
        var result = underTest.findRequestedFeedbackCount(COLLEAGUE_UUID);

        //then
        verify(feedbackDAO, times(1)).findRequestedFeedbackCount(COLLEAGUE_UUID);
        assertEquals(FEEDBACKS_COUNT, result);
    }

    @Test
    void updateFeedback() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        feedback.setStatus(FeedbackStatus.COMPLETED);
        when(feedbackDAO.update(eq(feedback), any())).thenReturn(1);

        //when
        Feedback result = underTest.update(feedback);

        //then
        assertEquals(FeedbackStatus.COMPLETED, result.getStatus());
    }

    @Test
    void insertFeedbackItem() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(1);

        //when
        FeedbackItem result = underTest.save(feedbackItem);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void updateFeedbackItem() {
        //given
        String content = "New content";
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        feedbackItem.setContent(content);
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(1);

        //when
        FeedbackItem result = underTest.save(feedbackItem);

        //then
        assertEquals(content, result.getContent());
    }

    @Test
    void saveFeedbackItemShouldThrowNotFoundException() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(0);

        //then
        assertThrows(NotFoundException.class, () -> underTest.save(feedbackItem));
    }

}