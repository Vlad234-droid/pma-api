package com.tesco.pma.feedback.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.api.FeedbackTargetType;
import com.tesco.pma.feedback.util.TestDataUtil;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeedbackDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/feedback/dao/";

    @Autowired
    private FeedbackDAO underTest;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void findAllFeedbacksWithItems() {
        //given
        RequestQuery requestQuery = new RequestQuery();

        //when
        List<Feedback> result = underTest.findAll(requestQuery);

        //then
        assertEquals(5, result.size());
        assertEquals(TestDataUtil.FEEDBACK_UUID_LAST, result.get(0).getUuid());
        assertEquals(3, result.get(0).getFeedbackItems().size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void getOneFeedbackByUuid() {
        //given

        //when
        Feedback result = underTest.getByUuid(TestDataUtil.FEEDBACK_UUID_LAST);

        //then
        assertEquals(TestDataUtil.FEEDBACK_UUID_LAST, result.getUuid());
        assertEquals(TestDataUtil.COLLEAGUE_UUID, result.getColleagueUuid());
        assertEquals(FeedbackTargetType.OBJECTIVE, result.getTargetType());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void findGivenFeedbackCount() {
        //given

        //when
        var result = underTest.findGivenFeedbackCount(TestDataUtil.COLLEAGUE_UUID);

        //then
        assertEquals(2, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void findRequestedFeedbackCount() {
        //given

        //when
        var result = underTest.findRequestedFeedbackCount(TestDataUtil.COLLEAGUE_UUID);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void markAsRead() {
        //given

        //when
        int result = underTest.markAsRead(TestDataUtil.FEEDBACK_UUID_UNREAD);
        Feedback readFeedback = underTest.getByUuid(TestDataUtil.FEEDBACK_UUID_UNREAD);

        //then
        assertEquals(1, result);
        assertTrue(readFeedback.getRead());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void insertFeedback() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(UUID.randomUUID());
        feedback.setCreatedTime(Instant.now());

        //when
        int result = underTest.insert(feedback);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void updateFeedback() {
        //given
        DictionaryFilter<FeedbackStatus> statusFilter = DictionaryFilter.includeFilter();
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);

        //when
        int result = underTest.update(feedback, statusFilter);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldThrowDuplicateKeyExceptionWhenInsertFeedbackHasAlreadyExistsUuid() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        feedback.setCreatedTime(Instant.now());

        //when and then
        var exception = assertThrows(DuplicateKeyException.class,
                () -> underTest.insert(feedback));
        assertTrue(exception.getMessage().contains(TestDataUtil.FEEDBACK_UUID_LAST.toString()));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void insertFeedbackItem() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(UUID.randomUUID());

        //when
        int result = underTest.insertOrUpdateFeedbackItem(feedbackItem);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void updateFeedbackItem() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);

        //when
        int result = underTest.insertOrUpdateFeedbackItem(feedbackItem);

        //then
        assertEquals(1, result);
    }

}
