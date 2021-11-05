package com.tesco.pma.review.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import static com.tesco.pma.review.domain.ReviewStatus.APPROVED;
import static com.tesco.pma.review.domain.ReviewStatus.COMPLETED;
import static com.tesco.pma.review.domain.ReviewStatus.DECLINED;
import static com.tesco.pma.review.domain.ReviewStatus.DRAFT;
import static com.tesco.pma.review.domain.ReviewStatus.WAITING_FOR_APPROVAL;

@Getter
@RequiredArgsConstructor
public enum ReviewType implements DictionaryItem<Integer> {

    OBJECTIVE(1, "Objective review") {
        @Override
        public List<ReviewStatus> getPrevStatusesForChangeStatus(ReviewStatus newStatus) {
            switch (newStatus) {
                case DRAFT:
                    return List.of(DRAFT);
                case WAITING_FOR_APPROVAL:
                    return List.of(DRAFT, WAITING_FOR_APPROVAL, DECLINED);
                case APPROVED:
                    return List.of(WAITING_FOR_APPROVAL, APPROVED);
                case DECLINED:
                    return List.of(WAITING_FOR_APPROVAL, DECLINED);
                case COMPLETED:
                    return List.of(APPROVED, COMPLETED);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<ReviewStatus> getStatusesForUpdate() {
            return List.of(DRAFT, DECLINED, APPROVED);
        }

        @Override
        public List<ReviewStatus> getStatusesForDelete() {
            return List.of(DRAFT, DECLINED, APPROVED);
        }
    },
    MYR(2, "Mid year review") {
        // TODO: 11/3/2021 should be implemented after receiving requirements
        @Override
        public List<ReviewStatus> getPrevStatusesForChangeStatus(ReviewStatus newStatus) {
            return null;
        }
    },
    EYR(3, "End of year review") {
        // TODO: 11/3/2021 should be implemented after receiving requirements
        @Override
        public List<ReviewStatus> getPrevStatusesForChangeStatus(ReviewStatus newStatus) {
            return null;
        }
    };

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }

    public abstract List<ReviewStatus> getPrevStatusesForChangeStatus(ReviewStatus newStatus);

    public List<ReviewStatus> getStatusesForCreate() {
        return List.of(DRAFT, WAITING_FOR_APPROVAL);
    }

    public List<ReviewStatus> getStatusesForUpdate() {
        return List.of(DRAFT, DECLINED);
    }

    public List<ReviewStatus> getStatusesForDelete() {
        return Collections.emptyList();
    }
}
