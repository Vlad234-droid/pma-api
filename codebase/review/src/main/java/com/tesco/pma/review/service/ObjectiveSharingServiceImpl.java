package com.tesco.pma.review.service;

import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.dao.ObjectiveSharingDAO;
import com.tesco.pma.review.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.review.exception.ErrorCodes.OBJECTIVE_SHARING_ALREADY_ENABLED;
import static com.tesco.pma.review.exception.ErrorCodes.OBJECTIVE_SHARING_NOT_ENABLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectiveSharingServiceImpl implements ObjectiveSharingService {

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";

    private static final String NF_OBJECTIVE_SHARING_START = "NF_OBJECTIVE_SHARING_START";
    private static final String NF_OBJECTIVE_SHARING_END = "NF_OBJECTIVE_SHARING_END";

    private static final String COLLEAGUE_UUID_EVENT_PARAM = "COLLEAGUE_UUID";

    private final ProfileService profileService;
    private final PMCycleService pmCycleService;
    private final ReviewService reviewService;
    private final ObjectiveSharingDAO dao;
    private final EventSender eventSender;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public void shareObjectives(UUID colleagueUuid, UUID cycleUuid) {
        try {
            dao.shareObjectives(colleagueUuid, cycleUuid);
        } catch (DuplicateKeyException e) {
            throw new DatabaseConstraintViolationException(OBJECTIVE_SHARING_ALREADY_ENABLED.getCode(),
                    messageSourceAccessor.getMessage(OBJECTIVE_SHARING_ALREADY_ENABLED,
                            Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                                    PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, cycleUuid)), null, e);

        }

        sendEventToSubordinates(NF_OBJECTIVE_SHARING_START, colleagueUuid);

    }

    @Override
    public void stopSharingObjectives(UUID colleagueUuid, UUID cycleUuid) {
        var removed = dao.stopSharingObjectives(colleagueUuid, cycleUuid);
        if (1 != removed) {
            throw new NotFoundException(OBJECTIVE_SHARING_NOT_ENABLED.getCode(),
                    messageSourceAccessor.getMessage(OBJECTIVE_SHARING_NOT_ENABLED,
                            Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                                    PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, cycleUuid)));
        }

        sendEventToSubordinates(NF_OBJECTIVE_SHARING_END, colleagueUuid);
    }

    @Override
    public boolean isColleagueShareObjectives(UUID colleagueUuid, UUID cycleUuid) {
        return dao.isColleagueShareObjectives(colleagueUuid, cycleUuid);
    }

    @Override
    public List<Review> getSharedObjectivesForColleague(UUID colleagueUuid) {
        var managerUuid = profileService.getColleague(colleagueUuid).getManagerUuid();
        if (managerUuid == null) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.COLLEAGUES_MANAGER_NOT_FOUND,
                    String.format("Colleague's %s manager is null", colleagueUuid)));
            return Collections.emptyList();
        }
        var cycle = pmCycleService.getCurrentByColleague(managerUuid);
        if (!isColleagueShareObjectives(managerUuid, cycle.getUuid())) {
            log.warn(LogFormatter.formatMessage(messageSourceAccessor, OBJECTIVE_SHARING_NOT_ENABLED,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, managerUuid, PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, cycle.getUuid())));
            return Collections.emptyList();
        }
        return reviewService.getReviews(cycle.getUuid(), managerUuid, PMReviewType.OBJECTIVE, PMTimelinePointStatus.APPROVED);
    }

    private void sendEventToSubordinates(String eventName, UUID managerUuid) {
        var rq = new RequestQuery();
        rq.addFilters("manager-uuid_eq", managerUuid);

        profileService.getSuggestions(rq)
                .forEach(colleague -> sendEvent(eventName, colleague.getColleague().getColleagueUUID()));
    }

    private void sendEvent(String eventName, UUID colleagueUuid) {
        var event = EventSupport.create(eventName,
                Map.of(COLLEAGUE_UUID_EVENT_PARAM, colleagueUuid));

        eventSender.sendEvent(event, null, true);
    }
}
