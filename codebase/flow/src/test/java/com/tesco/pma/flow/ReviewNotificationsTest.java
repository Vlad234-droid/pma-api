package com.tesco.pma.flow;

import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.flow.handlers.FlowParameters;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReviewNotificationsTest {

    private static final String PATH = "com/tesco/pma/flow/review_notifications.dmn";
    private static final String DMN_ID = "review_notifications";
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
        ColleagueProfile colleagueProfile = new ColleagueProfile();
        colleagueProfile.setProfileAttributes(List.of(createAttr(attrName, "true")));

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

        ColleagueProfile colleagueProfile = new ColleagueProfile();
        colleagueProfile.setProfileAttributes(List.of(createAttr("Some attr name", "false")));

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

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    private TypedAttribute createAttr(String name, String value) {
        var attr = new TypedAttribute();
        attr.setName(name);
        attr.setValue(value);
        return attr;
    }

}
