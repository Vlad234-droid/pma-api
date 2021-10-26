package com.tesco.pma.feedback.service.impl;

import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.dao.FeedbackItemDAO;
import com.tesco.pma.feedback.service.FeedbackItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        feedbackItem.setUuid(UUID.randomUUID());
        feedbackItemDAO.insert(feedbackItem);
        return feedbackItem;
    }

    @Override
    @Transactional
    public FeedbackItem update(FeedbackItem feedbackItem) {
        feedbackItemDAO.update(feedbackItem);
        return feedbackItem;
    }

}
