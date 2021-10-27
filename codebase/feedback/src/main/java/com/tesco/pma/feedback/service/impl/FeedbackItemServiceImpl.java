package com.tesco.pma.feedback.service.impl;

import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.dao.FeedbackItemDAO;
import com.tesco.pma.feedback.service.FeedbackItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

/**
 * Service Implementation for managing {@link FeedbackItem}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackItemServiceImpl implements FeedbackItemService {

    private final FeedbackItemDAO feedbackItemDAO;
    private final MessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public FeedbackItem create(FeedbackItem feedbackItem) {
        log.debug("Request to save FeedbackItem : {}", feedbackItem);
        try {
            feedbackItem.setUuid(UUID.randomUUID());
            feedbackItemDAO.insert(feedbackItem);
        } catch (DuplicateKeyException | ConstraintViolationException ex) {
            String message = messageSourceAccessor.getMessage(ErrorCodes.CONSTRAINT_VIOLATION.getCode());
            throw new DatabaseConstraintViolationException(com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), message, null, ex);
        }
        return feedbackItem;
    }

    @Override
    @Transactional
    public FeedbackItem update(FeedbackItem feedbackItem) {
        feedbackItemDAO.update(feedbackItem);
        return feedbackItem;
    }

}
