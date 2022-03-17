package com.tesco.pma.feedback.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
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
import java.util.Set;

import static com.tesco.pma.feedback.util.TestDataUtil.COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACKS_COUNT;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_ITEM_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_LAST;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_UNREAD;
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
        var feedback = TestDataUtil.buildFeedback();

        //when
        var result = underTest.create(feedback);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void shouldThrowDatabaseConstraintViolationExceptionWhenCreateFeedbackWithDuplicateKey() {
        //given
        var feedback = TestDataUtil.buildFeedback();
        when(feedbackDAO.insert(feedback)).thenThrow(DuplicateKeyException.class);

        //then
        assertThrows(DatabaseConstraintViolationException.class, () -> underTest.create(feedback));
    }

    @Test
    void markAsRead() {
        //when
        underTest.markAsRead(FEEDBACK_UUID_UNREAD, COLLEAGUE_UUID);

        //then
        verify(feedbackDAO, times(1)).markAsRead(FEEDBACK_UUID_UNREAD, COLLEAGUE_UUID);
    }

    @Test
    void findAllFeedbacks() {
        //given
        var requestQuery = new RequestQuery();
        var feedback1 = TestDataUtil.buildFeedback();
        feedback1.setUuid(FEEDBACK_UUID_LAST);
        var feedback2 = TestDataUtil.buildFeedback();
        feedback2.setUuid(FEEDBACK_UUID_UNREAD);
        var feedbacks = List.of(feedback1, feedback2);
        when(feedbackDAO.findAll(requestQuery)).thenReturn(feedbacks);

        //when
        var result = underTest.findAll(requestQuery);

        //then
        assertEquals(2, result.size());
        assertEquals(feedback1, result.get(0));
    }

    @Test
    void findOneFeedback() {
        //given
        var feedback = TestDataUtil.buildFeedback();
        when(feedbackDAO.getByUuid(FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when
        var result = underTest.findOne(FEEDBACK_UUID_LAST);

        //then
        verify(feedbackDAO, times(1)).getByUuid(FEEDBACK_UUID_LAST);
        assertEquals(COLLEAGUE_UUID, result.getColleagueUuid());
    }

    @Test
    void findGivenFeedbackCount() {
        //given
        var statusFilter = DictionaryFilter.includeFilter(Set.of(FeedbackStatus.SUBMITTED, FeedbackStatus.COMPLETED));
        when(feedbackDAO.getGivenFeedbackCount(COLLEAGUE_UUID, statusFilter)).thenReturn(FEEDBACKS_COUNT);

        //when
        var result = underTest.getGivenFeedbackCount(COLLEAGUE_UUID);

        //then
        verify(feedbackDAO, times(1)).getGivenFeedbackCount(COLLEAGUE_UUID, statusFilter);
        assertEquals(FEEDBACKS_COUNT, result);
    }

    @Test
    void findRequestedFeedbackCount() {
        //given
        var statusFilter = DictionaryFilter.includeFilter(Set.of(FeedbackStatus.PENDING));
        when(feedbackDAO.getRequestedFeedbackCount(COLLEAGUE_UUID, statusFilter)).thenReturn(FEEDBACKS_COUNT);

        //when
        var result = underTest.getRequestedFeedbackCount(COLLEAGUE_UUID);

        //then
        verify(feedbackDAO, times(1)).getRequestedFeedbackCount(COLLEAGUE_UUID, statusFilter);
        assertEquals(FEEDBACKS_COUNT, result);
    }

    @Test
    void updateFeedback() {
        //given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        feedback.setStatus(FeedbackStatus.COMPLETED);
        when(feedbackDAO.update(eq(feedback), any())).thenReturn(1);

        //when
        var result = underTest.update(feedback);

        //then
        assertEquals(FeedbackStatus.COMPLETED, result.getStatus());
    }

    @Test
    void insertFeedbackItem() {
        //given
        var feedbackItem = TestDataUtil.buildFeedbackItem();
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(1);

        //when
        var result = underTest.save(feedbackItem);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void updateFeedbackItem() {
        //given
        var content = "New content";
        var feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(FEEDBACK_ITEM_UUID);
        feedbackItem.setContent(content);
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(1);

        //when
        var result = underTest.save(feedbackItem);

        //then
        assertEquals(content, result.getContent());
    }

    @Test
    void saveFeedbackItemShouldThrowNotFoundException() {
        //given
        var feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(FEEDBACK_ITEM_UUID);
        when(feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)).thenReturn(0);

        //then
        assertThrows(NotFoundException.class, () -> underTest.save(feedbackItem));
    }

}