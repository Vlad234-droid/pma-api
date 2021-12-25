package com.tesco.pma.flow;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.cycle.api.PMReviewType;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReviewNotificationsTest {

    private static final String PATH = "com/tesco/pma/flow/review_notifications_decisions.dmn";
    private static final String DMN_ID = "review_notifications_decisions_table";
    private static final String PM_REVIEW_SUBMITTED = "PM_REVIEW_SUBMITTED";

    private DmnEngine dmnEngine;
    private DmnDecision decision;

    @BeforeEach
    public void init() throws IOException {
        dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PATH)) {
            decision = dmnEngine.parseDecision(DMN_ID, inputStream);
        }
    }

    @Test
    public void sendTest() {

        String attrName = "Attr name";
        var colleagueProfile = createColleagueProfile(null, WorkLevel.WL1, Map.of(attrName, "true"));

        VariableMap variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.REVIEW_TYPE.name(), PMReviewType.MYR.getCode());
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), attrName);
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    @Test
    public void sendTestWhenAttrNotExist() {
        var colleagueProfile = createColleagueProfile(null, WorkLevel.WL1, Map.of("Some attr name", "false"));

        VariableMap variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.REVIEW_TYPE.name(), PMReviewType.MYR.getCode());
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), "Attr name");
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    @Test
    public void sendTestWhenAttrsNull() {

        ColleagueProfile colleagueProfile = new ColleagueProfile();

        VariableMap variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.REVIEW_TYPE.name(), PMReviewType.MYR.getCode());
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), "Attr name");
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);
        variables.putValue(FlowParameters.COLLEAGUE_WORK_LEVEL.name(), WorkLevel.WL1.name());

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    private ColleagueProfile createColleagueProfile(UUID colleagueUUID, WorkLevel wl, Map<String, String> attrs){
        var wr = new WorkRelationship();
        wr.setWorkLevel(wl);

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUUID);
        colleague.setWorkRelationships(List.of(wr));

        ColleagueProfile colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(colleague);
        colleagueProfile.setProfileAttributes(new ArrayList<>());
        attrs.forEach((k, v) -> colleagueProfile.getProfileAttributes().add(createAttr(k, v)));
        return colleagueProfile;
    }

    private TypedAttribute createAttr(String name, String value) {
        var attr = new TypedAttribute();
        attr.setName(name);
        attr.setValue(value);
        return attr;
    }

}
