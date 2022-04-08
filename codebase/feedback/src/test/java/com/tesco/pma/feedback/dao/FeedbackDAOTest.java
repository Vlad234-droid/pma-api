package com.tesco.pma.feedback.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.api.FeedbackTargetType;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.feedback.util.TestDataUtil.COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_ITEM_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_LAST;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_UNREAD;
import static com.tesco.pma.feedback.util.TestDataUtil.FIRST_NAME;
import static com.tesco.pma.feedback.util.TestDataUtil.TARGET_COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.buildFeedback;
import static com.tesco.pma.feedback.util.TestDataUtil.buildFeedbackItem;
import static com.tesco.pma.pagination.Condition.Operand.CONTAINS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackDAOTest extends AbstractDAOTest {

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
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition("colleague-first-name", CONTAINS, "O'tes")));

        //when
        var result = underTest.findAll(requestQuery.toDAO());

        //then
        assertEquals(3, result.size());
        assertEquals(FEEDBACK_UUID_LAST, result.get(0).getUuid());
        assertEquals(FIRST_NAME, result.get(0).getColleagueProfile().getColleague().getProfile().getFirstName());
        assertEquals(3, result.get(0).getFeedbackItems().size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void getOneFeedbackByUuid() {
        //given

        //when
        var result = underTest.getByUuid(FEEDBACK_UUID_LAST);

        //then
        assertEquals(FEEDBACK_UUID_LAST, result.getUuid());
        assertEquals(COLLEAGUE_UUID, result.getColleagueUuid());
        assertEquals(FeedbackTargetType.OBJECTIVE, result.getTargetType());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void findGivenFeedbackCount() {
        //given

        //when
        var result = underTest.getGivenFeedbackCount(COLLEAGUE_UUID,
                DictionaryFilter.includeFilter(Set.of(FeedbackStatus.SUBMITTED, FeedbackStatus.COMPLETED)));

        //then
        assertEquals(3, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void findRequestedFeedbackCount() {
        //given

        //when
        var result = underTest.getRequestedFeedbackCount(COLLEAGUE_UUID,
                DictionaryFilter.includeFilter(Set.of(FeedbackStatus.PENDING)));

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void markAsRead() {
        //given

        //when
        var result = underTest.markAsRead(FEEDBACK_UUID_UNREAD, TARGET_COLLEAGUE_UUID);
        var readFeedback = underTest.getByUuid(FEEDBACK_UUID_UNREAD);

        //then
        assertEquals(1, result);
        assertTrue(readFeedback.getRead());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void insertFeedback() {
        //given
        var feedback = buildFeedback();
        feedback.setUuid(UUID.randomUUID());
        feedback.setCreatedTime(Instant.now());

        //when
        var result = underTest.insert(feedback);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void updateFeedback() {
        //given
        DictionaryFilter<FeedbackStatus> statusFilter = DictionaryFilter.includeFilter();
        var feedback = buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);

        //when
        var result = underTest.update(feedback, statusFilter);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldThrowDuplicateKeyExceptionWhenInsertFeedbackHasAlreadyExistsUuid() {
        //given
        var feedback = buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        feedback.setCreatedTime(Instant.now());

        //when and then
        var exception = assertThrows(DuplicateKeyException.class,
                () -> underTest.insert(feedback));
        assertTrue(exception.getMessage().contains(FEEDBACK_UUID_LAST.toString()));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void insertFeedbackItem() {
        //given
        var feedbackItem = buildFeedbackItem();
        feedbackItem.setUuid(UUID.randomUUID());

        //when
        var result = underTest.insertOrUpdateFeedbackItem(feedbackItem);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void updateFeedbackItem() {
        //given
        var feedbackItem = buildFeedbackItem();
        feedbackItem.setUuid(FEEDBACK_ITEM_UUID);

        //when
        var result = underTest.insertOrUpdateFeedbackItem(feedbackItem);

        //then
        assertEquals(1, result);
    }

}
