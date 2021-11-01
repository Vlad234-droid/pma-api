package com.tesco.pma.feedback.service.impl;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.dao.FeedbackDAO;
import com.tesco.pma.feedback.exception.ErrorCodes;
import com.tesco.pma.feedback.service.FeedbackItemService;
import com.tesco.pma.feedback.service.FeedbackService;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final FeedbackDAO feedbackDAO;
    private final FeedbackItemService feedbackItemService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public Feedback create(Feedback feedback) {
        log.debug("Request to save Feedback : {}", feedback);
        try {
            feedback.setUuid(UUID.randomUUID());
            feedbackDAO.insert(feedback);
            Set<FeedbackItem> feedbackItems = feedback.getFeedbackItems()
                    .stream()
                    .peek(feedbackItem -> feedbackItem.setFeedbackUuid(feedback.getUuid()))
                    .map(feedbackItemService::create)
                    .collect(Collectors.toSet());
            feedback.setFeedbackItems(feedbackItems);
        } catch (DuplicateKeyException ex) {
            String message = messageSourceAccessor.getMessage(com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION);
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), message, null, ex);
        }
        return feedback;
    }

    @Override
    @Transactional
    public Feedback update(Feedback feedback) {
        DictionaryFilter<FeedbackStatus> statusFilter = UPDATE_STATUS_RULE_MAP.get(feedback.getStatus());
        if (1 == feedbackDAO.update(feedback, statusFilter)) {
            for (FeedbackItem feedbackItem : feedback.getFeedbackItems()) {
                feedbackItem.setFeedbackUuid(feedback.getUuid());
            }
            Set<FeedbackItem> nonExistFeedbackItems = feedback.getFeedbackItems()
                    .stream()
                    .filter(feedbackItem -> Objects.isNull(feedbackItem.getUuid()))
                    .map(feedbackItemService::create)
                    .collect(Collectors.toSet());
            Set<FeedbackItem> feedbackItems = feedback.getFeedbackItems()
                    .stream()
                    .filter(feedbackItem -> Objects.nonNull(feedbackItem.getUuid()))
                    .map(feedbackItemService::update)
                    .collect(Collectors.toSet());
            feedbackItems.addAll(nonExistFeedbackItems);
            feedback.setFeedbackItems(feedbackItems);
        } else {
            String message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_NOT_FOUND,
                    Map.of(PARAM_NAME, "uuid", PARAM_VALUE, feedback.getUuid()));
            throw new NotFoundException(ErrorCodes.FEEDBACK_NOT_FOUND.getCode(), message);
        }
        return feedback;
    }

    @Override
    @Transactional
    public void markAsRead(UUID uuid) {
        log.debug("Request to mark as read Feedback with uuid: {}", uuid);
        feedbackDAO.markAsRead(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> findAll(RequestQuery requestQuery) {
        log.debug("Request to get all Feedbacks");
        return feedbackDAO.findAll(requestQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public Feedback findOne(UUID uuid) {
        log.debug("Request to get Feedback : {}", uuid);
        Feedback feedback = feedbackDAO.getByUuid(uuid);
        if (feedback == null) {
            String message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_NOT_FOUND,
                    Map.of(PARAM_NAME, "uuid", PARAM_VALUE, uuid));
            throw new NotFoundException(ErrorCodes.FEEDBACK_NOT_FOUND.getCode(), message);
        }
        return feedback;
    }

}
