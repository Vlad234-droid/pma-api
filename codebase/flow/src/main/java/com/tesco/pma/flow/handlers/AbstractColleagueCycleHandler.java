package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;

import java.time.LocalDate;
import java.util.UUID;

import static com.tesco.pma.flow.handlers.HandlerUtils.instantToDate;

public abstract class AbstractColleagueCycleHandler extends CamundaAbstractFlowHandler {

    protected LocalDate defineStartDate(PMCycle cycle) {
        return PMCycleType.HIRING == cycle.getType() ? LocalDate.now() : instantToDate(cycle.getStartTime());
    }

    protected PMColleagueCycle mapToColleagueCycle(UUID colleagueUuid, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleagueUuid);
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        var startTime = cycle.getStartTime();
        var endTime = cycle.getEndTime();
        if (PMCycleType.HIRING == cycle.getType()) {
            startTime = HandlerUtils.dateToInstant(LocalDate.now());
            endTime = HandlerUtils.dateToInstant(LocalDate.now().plusYears(1));
        }
        cc.setStartTime(startTime);
        cc.setEndTime(endTime);
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
