package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public abstract class AbstractColleagueCycleHandler extends CamundaAbstractFlowHandler {
    /**
     * If cycle type is HIRING the start and end time are adjusted to current date
     *
     * @param cycle PM cycle
     * @return adjusted cycle
     */
    protected PMCycle adjustStartDate(PMCycle cycle) {
        if (PMCycleType.HIRING.equals(cycle.getType())) {
            var start = Instant.now(Clock.systemUTC());
            cycle.setStartTime(start);
            cycle.setEndTime(start.plus(Period.ofYears(1)));
        }
        return cycle;
    }

    protected PMColleagueCycle mapToColleagueCycle(UUID colleagueUuid, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleagueUuid);
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        cc.setStartTime(cycle.getStartTime());
        cc.setEndTime(cycle.getEndTime());
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
