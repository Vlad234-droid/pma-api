package com.tesco.pma.cycle.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.service.ProfileService;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PMCycleMappingServiceTest {

    private final ProfileService profileService = Mockito.mock(ProfileService.class);
    private final ProcessEngine processEngine = Mockito.mock(ProcessEngine.class);

    private final PMCycleMappingServiceImpl pmCycleMappingService = new PMCycleMappingServiceImpl(profileService, processEngine);

    @Test
    void getPmCycleMappingKeysTest() {
        var wl = new WorkRelationship();
        var colleague = new Colleague();
        colleague.setColleagueUUID(UUID.randomUUID());
        colleague.setWorkRelationships(List.of(wl));

        Mockito.when(profileService.findColleagueByColleagueUuid(colleague.getColleagueUUID())).thenReturn(colleague);

        var groupRes = "Group A";
        var result = Mockito.mock(DmnDecisionTableResult.class);
        Mockito.when(result.getSingleEntry()).thenReturn(groupRes);
        var decisionService = Mockito.mock(DecisionService.class);
        Mockito.when(decisionService.evaluateDecisionTableByKey(Mockito.anyString(), Mockito.anyMap())).thenReturn(result);
        Mockito.when(processEngine.getDecisionService()).thenReturn(decisionService);

        var resMap = pmCycleMappingService.getPmCycleMappingKeys(List.of(colleague.getColleagueUUID()));

        assertEquals(groupRes, resMap.get(colleague.getColleagueUUID()));

    }


}
