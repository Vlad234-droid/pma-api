package com.tesco.pma.feedback.service.impl;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.dao.FeedbackItemDAO;
import com.tesco.pma.feedback.exception.ErrorCodes;
import com.tesco.pma.feedback.service.FeedbackItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Service Implementation for managing {@link FeedbackItem}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackItemServiceImpl implements FeedbackItemService {

    private static final String PARAM_NAME = "paramName";
    private static final String PARAM_VALUE = "paramValue";

    private final FeedbackItemDAO feedbackItemDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public FeedbackItem save(FeedbackItem feedbackItem) {
        log.debug("Request to save FeedbackItem : {}", feedbackItem);
        feedbackItem.setUuid(UUID.randomUUID());
        if (1 != feedbackItemDAO.save(feedbackItem)) {
            String message = messageSourceAccessor.getMessage(ErrorCodes.FEEDBACK_ITEM_NOT_FOUND,
                    Map.of(PARAM_NAME, "uuid", PARAM_VALUE, feedbackItem.getUuid()));
            throw new NotFoundException(ErrorCodes.FEEDBACK_ITEM_NOT_FOUND.getCode(), message);
        }
        return feedbackItem;
    }

}
