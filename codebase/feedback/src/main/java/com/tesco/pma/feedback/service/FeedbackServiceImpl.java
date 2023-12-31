package com.tesco.pma.feedback.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.dao.FeedbackDAO;
import com.tesco.pma.feedback.exception.ErrorCodes;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Feedback}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private static final String PARAM_NAME = "paramName";
    private static final String PARAM_VALUE = "paramValue";
    private static final String COLLEAGUE_UUID_PARAM_NAME = "COLLEAGUE_UUID";
    private static final String SOURCE_COLLEAGUE_UUID_PARAM_NAME = "SOURCE_COLLEAGUE_UUID";
    private static final String NF_FEEDBACK_GIVEN = "NF_FEEDBACK_GIVEN";
    private static final String NF_RESPOND_TO_FEEDBACK_REQUESTS = "NF_FEEDBACK_REQUESTS_RESPONDED";
    private static final String NF_REQUEST_FEEDBACK = "NF_FEEDBACK_REQUESTED";
    private static final Map<FeedbackStatus, DictionaryFilter<FeedbackStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                FeedbackStatus.DRAFT, DictionaryFilter.excludeFilter(
                        Set.of(FeedbackStatus.SUBMITTED, FeedbackStatus.PENDING, FeedbackStatus.COMPLETED)),
                FeedbackStatus.SUBMITTED, DictionaryFilter.includeFilter(Set.of(FeedbackStatus.DRAFT)),
                FeedbackStatus.PENDING, DictionaryFilter.excludeFilter(
                        Set.of(FeedbackStatus.DRAFT, FeedbackStatus.SUBMITTED, FeedbackStatus.COMPLETED)),
                FeedbackStatus.COMPLETED, DictionaryFilter.includeFilter(Set.of(FeedbackStatus.PENDING))
        );
    }

    private static final Map<FeedbackStatus, String> FEEDBACK_STATUS_TO_EVENT_NAME_MAP = Map.of(
            FeedbackStatus.SUBMITTED, NF_FEEDBACK_GIVEN,
            FeedbackStatus.PENDING, NF_REQUEST_FEEDBACK,
            FeedbackStatus.COMPLETED, NF_RESPOND_TO_FEEDBACK_REQUESTS
    );

    private final FeedbackDAO feedbackDAO;
    private final EventSender eventSender;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public Feedback create(Feedback feedback) {
        log.debug("Request to save Feedback : {}", feedback);
        try {
            feedback.setUuid(UUID.randomUUID());
            feedback.setCreatedTime(Instant.now());
            feedback.setUpdatedTime(Instant.now());
            feedbackDAO.insert(feedback);

            Set<FeedbackItem> feedbackItems = feedback.getFeedbackItems()
                    .stream()
                    .map(feedbackItem -> {
                        feedbackItem.setFeedbackUuid(feedback.getUuid());
                        return feedbackItem;
                    })
                    .map(this::save)
                    .collect(Collectors.toSet());

            feedback.setFeedbackItems(feedbackItems);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
        }

        sendEvent(feedback);
        return feedback;
    }

    @Override
    @Transactional
    public Feedback update(Feedback feedback) {
        DictionaryFilter<FeedbackStatus> statusFilter = UPDATE_STATUS_RULE_MAP.get(feedback.getStatus());
        feedback.setUpdatedTime(Instant.now());
        try {
            if (1 == feedbackDAO.update(feedback, statusFilter)) {
                Set<FeedbackItem> feedbackItems = feedback.getFeedbackItems()
                        .stream()
                        .map(feedbackItem -> {
                            feedbackItem.setFeedbackUuid(feedback.getUuid());
                            return feedbackItem;
                        })
                        .map(this::save)
                        .collect(Collectors.toSet());
                feedback.setFeedbackItems(feedbackItems);
            } else {
                String message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_NOT_FOUND,
                        Map.of(PARAM_NAME, "uuid", PARAM_VALUE, feedback.getUuid()));
                throw new NotFoundException(ErrorCodes.FEEDBACK_NOT_FOUND.getCode(), message);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
        }

        sendEvent(feedback);
        return feedback;
    }

    @Override
    @Transactional
    public void markAsRead(UUID uuid, UUID colleagueUuid) {
        log.debug("Request to mark as read Feedback with uuid: {} for colleague or target uuid: {}", uuid, colleagueUuid);
        feedbackDAO.markAsRead(uuid, colleagueUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> findAll(RequestQuery requestQuery) {
        log.debug("Request to get all Feedbacks");
        return feedbackDAO.findAll(requestQuery.toDAO());
    }

    @Override
    @Transactional(readOnly = true)
    public Feedback findOne(UUID uuid) {
        log.debug("Request to get Feedback : {}", uuid);
        var feedback = feedbackDAO.getByUuid(uuid);
        if (feedback == null) {
            var message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_NOT_FOUND,
                    Map.of(PARAM_NAME, "uuid", PARAM_VALUE, uuid));
            throw new NotFoundException(ErrorCodes.FEEDBACK_NOT_FOUND.getCode(), message);
        }
        return feedback;
    }

    @Override
    @Transactional(readOnly = true)
    public int getGivenFeedbackCount(UUID colleagueUuid) {
        log.debug("Request to get given feedbacks count for colleague: {}", colleagueUuid);
        return feedbackDAO.getGivenFeedbackCount(colleagueUuid,
                DictionaryFilter.includeFilter(Set.of(FeedbackStatus.SUBMITTED, FeedbackStatus.COMPLETED)));
    }

    @Override
    @Transactional(readOnly = true)
    public int getRequestedFeedbackCount(UUID colleagueUuid) {
        log.debug("Request to get requested feedbacks count for colleague: {}", colleagueUuid);
        return feedbackDAO.getRequestedFeedbackCount(colleagueUuid,
                DictionaryFilter.includeFilter(Set.of(FeedbackStatus.PENDING)));
    }

    @Override
    public FeedbackItem save(FeedbackItem feedbackItem) {
        log.debug("Request to save FeedbackItem : {}", feedbackItem);
        if (feedbackItem.getUuid() == null) {
            feedbackItem.setUuid(UUID.randomUUID());
        }
        if (1 != feedbackDAO.insertOrUpdateFeedbackItem(feedbackItem)) {
            var message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_ITEM_NOT_FOUND,
                    Map.of(PARAM_NAME, "uuid", PARAM_VALUE, feedbackItem.getUuid()));
            throw new NotFoundException(ErrorCodes.FEEDBACK_ITEM_NOT_FOUND.getCode(), message);
        }
        return feedbackItem;
    }

    private void sendEvent(Feedback feedback) {
        var eventName = FEEDBACK_STATUS_TO_EVENT_NAME_MAP.get(feedback.getStatus());

        if (eventName == null) {
            return;
        }

        var target = feedback.getTargetColleagueUuid();
        var source = feedback.getColleagueUuid();

        if (eventName.equals(NF_REQUEST_FEEDBACK)) {
            target = feedback.getColleagueUuid();
            source = feedback.getTargetColleagueUuid();
        }

        var event = EventSupport.create(eventName, Map.of(
                COLLEAGUE_UUID_PARAM_NAME, target,
                SOURCE_COLLEAGUE_UUID_PARAM_NAME, source
        ));

        eventSender.sendEvent(event, null, true);
    }

}
