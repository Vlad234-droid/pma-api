package com.tesco.pma.feedback.service.impl;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.dao.FeedbackDAO;
import com.tesco.pma.feedback.service.FeedbackItemService;
import com.tesco.pma.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Feedback}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackDAO feedbackDAO;
    private final FeedbackItemService feedbackItemService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public Feedback create(Feedback feedback) {
        log.debug("Request to save Feedback : {}", feedback);
        if (1 == feedbackDAO.insert(feedback)) {
            Set<FeedbackItem> feedbackItems = feedback.getFeedbackItems()
                    .stream()
                    .peek(feedbackItem -> feedbackItem.setFeedbackId(feedback.getId()))
                    .map(feedbackItemService::create)
                    .collect(Collectors.toSet());
            feedback.setFeedbackItems(feedbackItems);
        } else {
            String message = messageSourceAccessor.getMessage(ErrorCodes.CONSTRAINT_VIOLATION);
            throw new DatabaseConstraintViolationException(ErrorCodes.CONSTRAINT_VIOLATION.getCode(), message, null);
        }
        return feedback;
    }

    @Override
    public void markAsRead(Long feedbackId) {
        log.debug("Request to mark as read feedback with id: {}", feedbackId);
        feedbackDAO.markAsRead(feedbackId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        log.debug("Request to get all Feedbacks");
        return feedbackDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Feedback> findOne(Long id) {
        log.debug("Request to get Feedback : {}", id);
        return Optional.ofNullable(feedbackDAO.getById(id));
    }

}
