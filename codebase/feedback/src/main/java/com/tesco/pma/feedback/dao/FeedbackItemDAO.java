package com.tesco.pma.feedback.dao;

import com.tesco.pma.feedback.api.FeedbackItem;
import org.apache.ibatis.annotations.Param;

public interface FeedbackItemDAO {

    /**
     * Insert feedback item
     *
     * @param feedbackItem a FeedbackItem
     * @return number of inserted entities
     */
    int insert(@Param("feedbackItem") FeedbackItem feedbackItem);

}
