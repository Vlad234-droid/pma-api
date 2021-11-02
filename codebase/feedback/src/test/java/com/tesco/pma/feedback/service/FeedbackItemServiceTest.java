package com.tesco.pma.feedback.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
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
    void insert() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        when(feedbackItemDAO.save(feedbackItem)).thenReturn(1);

        //when
        FeedbackItem result = underTest.save(feedbackItem);

        //then
        assertNotNull(result.getUuid());
    }

    @Test
    void update() {
        //given
        String content = "New content";
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        feedbackItem.setContent(content);
        when(feedbackItemDAO.save(feedbackItem)).thenReturn(1);

        //when
        FeedbackItem result = underTest.save(feedbackItem);

        //then
        assertEquals(result.getContent(), content);
    }

    @Test
    void saveShouldThrowNotFoundException() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);
        when(feedbackItemDAO.save(feedbackItem)).thenReturn(0);

        //then
        assertThrows(NotFoundException.class, () -> underTest.save(feedbackItem));
    }
}