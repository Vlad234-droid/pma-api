package com.tesco.pma.flow.notifications;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
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


class ReviewNotificationsDmnTest {

    private static final String PATH = "com/tesco/pma/flow/notifications/review/review_notifications_decisions.dmn";
    private static final String DMN_ID = "review_notifications_decisions_table";
    private static final String NF_PM_REVIEW_SUBMITTED = "NF_PM_REVIEW_SUBMITTED";

    private DmnEngine dmnEngine;
    private DmnDecision decision;
    private TimelinePoint timelinePoint;

    @BeforeEach
    void init() throws IOException {
        dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PATH)) {
            decision = dmnEngine.parseDecision(DMN_ID, inputStream);
        }

        timelinePoint = new TimelinePoint();
        timelinePoint.setCode("MYR");
    }

    @Test
    void sendTest() {
        var attrName = "Attr name";
        var colleagueProfile = createColleagueProfile(null, WorkLevel.WL1, Map.of(attrName, "true"));

        var variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), NF_PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.TIMELINE_POINT.name(), timelinePoint);
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), attrName);
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);

        var result = dmnEngine.evaluateDecisionTable(decision, (VariableMap) variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    @Test
    void sendTestWhenAttrNotExist() {
        var timelinePoint = new TimelinePoint();
        timelinePoint.setCode("MYR");

        var variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), NF_PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.TIMELINE_POINT.name(), timelinePoint);
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), "Attr name");
        var colleagueProfile = createColleagueProfile(null, WorkLevel.WL1, Map.of("Some attr name", "false"));
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);

        var result = dmnEngine.evaluateDecisionTable(decision, (VariableMap) variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    @Test
    void sendTestWhenAttrsNull() {
        var variables = new VariableMapImpl();
        variables.putValue(FlowParameters.EVENT_NAME.name(), NF_PM_REVIEW_SUBMITTED);
        variables.putValue(FlowParameters.TIMELINE_POINT.name(), timelinePoint);
        variables.putValue(FlowParameters.IS_MANAGER.name(), true);
        variables.putValue(FlowParameters.PROFILE_ATTRIBUTE_NAME.name(), "Attr name");
        var colleagueProfile = new ColleagueProfile();
        variables.putValue(FlowParameters.COLLEAGUE_PROFILE.name(), colleagueProfile);
        variables.putValue(FlowParameters.COLLEAGUE_WORK_LEVEL.name(), WorkLevel.WL1.name());

        var result = dmnEngine.evaluateDecisionTable(decision, (VariableMap) variables);

        assertTrue((Boolean) result.getFirstResult().getEntry(FlowParameters.SEND.name()));
    }

    private ColleagueProfile createColleagueProfile(UUID colleagueUUID, WorkLevel wl, Map<String, String> attrs) {
        var wr = new WorkRelationship();
        wr.setWorkLevel(wl);

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUUID);
        colleague.setWorkRelationships(List.of(wr));

        var colleagueProfile = new ColleagueProfile();
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
