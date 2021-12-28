package com.tesco.pma.pdp.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pdp.dao.PDPDao;
import com.tesco.pma.pdp.domain.PDPGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION;
import static com.tesco.pma.pdp.api.PDPGoalStatus.PUBLISHED;
import static com.tesco.pma.pdp.exception.ErrorCodes.PDP_ALREADY_EXISTS;
import static com.tesco.pma.pdp.exception.ErrorCodes.PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER;
import static com.tesco.pma.pdp.exception.ErrorCodes.PDP_GOAL_NOT_FOUND_BY_ID;

/**
 * Personal Development Plan service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class PDPServiceImpl implements PDPService {

    private static final String COLLEAGUE_UUID = "colleagueUuid";
    private static final String NUMBER = "number";
    private static final String GOAL_UUID = "goalUuid";

    private final PDPDao pdpDao;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    @Transactional
    public List<PDPGoal> createGoals(UUID colleagueUuid, List<PDPGoal> goals) {
        goals.forEach(goal -> {
            try {
                goal.setStatus(PUBLISHED);
                pdpDao.createGoal(goal);
            } catch (DuplicateKeyException ex) {
                throw new DatabaseConstraintViolationException(PDP_ALREADY_EXISTS.name(),
                        messageSourceAccessor.getMessage(PDP_ALREADY_EXISTS,
                                Map.of(COLLEAGUE_UUID, colleagueUuid, NUMBER, goal.getNumber())), null, ex);
            }
        });
        return goals;
    }

    @Override
    @Transactional
    public List<PDPGoal> updateGoals(UUID colleagueUuid, List<PDPGoal> goals) {
        goals.forEach(goal -> {
            int updated;
            try {
                updated = pdpDao.updateGoal(colleagueUuid, goal);
            } catch (DataIntegrityViolationException ex) {
                throw new DatabaseConstraintViolationException(CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
            }
            if (1 != updated) {
                throw new NotFoundException(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER.getCode(),
                        messageSourceAccessor.getMessage(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER,
                                Map.of(COLLEAGUE_UUID, colleagueUuid, NUMBER, goal.getNumber())));
            }
        });
        return goals;
    }

    @Override
    @Transactional
    public void deleteGoals(List<Integer> numbers, UUID colleagueUuid) {
        numbers.forEach(number -> {
            var deleted = pdpDao.deleteGoalByColleagueAndNumber(colleagueUuid, number);
            if (1 != deleted) {
                throw new NotFoundException(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER.getCode(),
                        messageSourceAccessor.getMessage(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER,
                                Map.of(COLLEAGUE_UUID, colleagueUuid, NUMBER, number)));
            }
        });
    }

    @Override
    @Transactional
    public void deleteGoals(UUID colleagueUuid, List<UUID> goalUuids) {
        goalUuids.forEach(goalUuid -> {
            var deleted = pdpDao.deleteGoalByUuidAndColleague(colleagueUuid, goalUuid);
            if (1 != deleted) {
                throw new NotFoundException(PDP_GOAL_NOT_FOUND_BY_ID.getCode(),
                        messageSourceAccessor.getMessage(PDP_GOAL_NOT_FOUND_BY_ID,
                                Map.of(GOAL_UUID, goalUuid)));
            }
        });
    }

    @Override
    public PDPGoal getGoal(UUID colleagueUuid, Integer number) {
        return Optional.ofNullable(pdpDao.readGoalByColleagueAndNumber(colleagueUuid, number))
                .orElseThrow(() -> new NotFoundException(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER.getCode(),
                        messageSourceAccessor.getMessage(PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER,
                                Map.of(COLLEAGUE_UUID, colleagueUuid, NUMBER, number))));
    }

    @Override
    public PDPGoal getGoal(UUID colleagueUuid, UUID goalUuid) {
        return Optional.ofNullable(pdpDao.readGoalByUuid(colleagueUuid, goalUuid))
                .orElseThrow(() -> new NotFoundException(PDP_GOAL_NOT_FOUND_BY_ID.getCode(),
                        messageSourceAccessor.getMessage(PDP_GOAL_NOT_FOUND_BY_ID,
                                Map.of(GOAL_UUID, goalUuid))));
    }

    @Override
    public List<PDPGoal> getGoals(UUID colleagueUuid) {
        return pdpDao.readGoalsByColleague(colleagueUuid);
    }
}