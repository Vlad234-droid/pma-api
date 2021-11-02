package com.tesco.pma.feedback.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.dao.FeedbackItemDAO;
import com.tesco.pma.feedback.service.impl.FeedbackItemServiceImpl;
import com.tesco.pma.feedback.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FeedbackItemServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class FeedbackItemServiceTest {

    @Autowired
    private FeedbackItemService underTest;

    @MockBean
    private FeedbackItemDAO feedbackItemDAO;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Test
    void create() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();

        //when
        FeedbackItem result = underTest.create(feedbackItem);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void createShouldThrowDatabaseConstraintViolationException() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        when(feedbackItemDAO.insert(feedbackItem)).thenThrow(DuplicateKeyException.class);

        //then
        assertThrows(DatabaseConstraintViolationException.class, () -> underTest.create(feedbackItem));
    }

    @Test
    void update() {
        //given
        String content = "New content";
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        feedbackItem.setContent(content);
        when(feedbackItemDAO.update(feedbackItem)).thenReturn(1);

        //when
        FeedbackItem result = underTest.update(feedbackItem);

        //then
        assertEquals(result.getContent(), content);
    }

    @Test
    void updateShouldThrowNotFoundException() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        when(feedbackItemDAO.update(feedbackItem)).thenReturn(0);

        //then
        assertThrows(NotFoundException.class, () -> underTest.update(feedbackItem));
    }
}