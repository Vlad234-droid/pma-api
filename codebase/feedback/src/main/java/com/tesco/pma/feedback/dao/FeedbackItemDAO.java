package com.tesco.pma.feedback.dao;

import com.tesco.pma.feedback.api.FeedbackItem;
import org.apache.ibatis.annotations.Param;

public interface FeedbackItemDAO {

    /**
     * Upsert feedback item
     *
     * @param feedbackItem a FeedbackItem
     * @return number of inserted entities
     */
    int save(@Param("feedbackItem") FeedbackItem feedbackItem);

}
