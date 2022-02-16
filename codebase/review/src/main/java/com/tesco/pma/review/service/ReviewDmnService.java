package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;

import java.util.List;

public interface ReviewDmnService {
    List<String> getReviewAllowedStatuses(PMReviewType reviewType, String operation);
}
