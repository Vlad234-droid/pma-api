package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.flow.FlowParameters;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class UpsertColleagueHandlerTest {

    private final ProfileService profileService = mock(ProfileService.class);
    private final UpsertColleagueHandler handler = new UpsertColleagueHandler(profileService);

    @Test
    void execute() throws Exception {
        var colleague = new Colleague();
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.COLLEAGUE, colleague)
                .build();

        handler.execute(executionContext);

        verify(profileService).updateColleague(colleague);
    }
}