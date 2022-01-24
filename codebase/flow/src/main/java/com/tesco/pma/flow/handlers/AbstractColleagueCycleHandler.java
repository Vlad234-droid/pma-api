package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;

import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public abstract class AbstractColleagueCycleHandler extends CamundaAbstractFlowHandler {

    protected PMColleagueCycle mapToColleagueCycle(ColleagueEntity colleague, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleague.getUuid());
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        Instant startTime = cycle.getStartTime();
        Instant endTime = cycle.getEndTime();
        if (PMCycleType.HIRING.equals(cycle.getType())) {
            startTime = HandlerUtils.dateToInstant(colleague.getHireDate());
            endTime = HandlerUtils.dateToInstant(colleague.getHireDate().plus(Period.ofYears(1)));
        }
        cc.setStartTime(startTime);
        cc.setEndTime(endTime);
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
