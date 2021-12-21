package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

public abstract class AbstractColleagueCycleHandler extends CamundaAbstractFlowHandler {

    protected LocalDate defineStartDate(PMCycle cycle) {
        LocalDate startDate = LocalDate.ofInstant(cycle.getStartTime(), ZoneId.systemDefault());
        if (PMCycleType.HIRING == cycle.getType()) {
            startDate = LocalDate.now();
        }
        return startDate;
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
            startTime = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
            endTime = LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        cc.setStartTime(startTime);
        cc.setEndTime(endTime);
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
