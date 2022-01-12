package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.flow.FlowParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitObjectiveNotificationHandler extends AbstractInitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        context.setVariable(FlowParameters.COLLEAGUE_WORK_LEVEL,
                getWorkLevel(context.getVariable(FlowParameters.COLLEAGUE_PROFILE)));
    }

    protected WorkLevel getWorkLevel(ColleagueProfile colleagueProfile) {
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getWorkLevel();
    }

}