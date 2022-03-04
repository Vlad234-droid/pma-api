package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.dao.TimelinePointDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class InitTimelinePointNotificationHandler extends AbstractInitNotificationHandler {

    private static final DateTimeFormatter TIMELINE_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MESSAGE_DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final TimelinePointDAO timelinePointDAO;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        var timelineUUID = (UUID) context.getEvent().getEventProperty(FlowParameters.TIMELINE_POINT_UUID.name());
        var timelinePoint = timelinePointDAO.getTimelineByUUID(timelineUUID);
        context.setVariable(FlowParameters.TIMELINE_POINT, timelinePoint);

        if (timelinePoint.getProperties() == null) {
            return;
        }

        var dateString = timelinePoint.getProperties().getMapJson().get(FlowParameters.START_DATE.name());
        var date = LocalDate.parse(dateString, TIMELINE_DTF).atStartOfDay();
        var today = LocalDate.now().atStartOfDay();
        long daysBetween = Duration.between(date, today).toDays();
        context.setVariable(FlowParameters.DAYS, daysBetween);
        context.setVariable(FlowParameters.START_DATE_S, date.format(MESSAGE_DTF));
    }

}