package com.tesco.pma.cycle.model;

import com.tesco.pma.api.ReviewType;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.LocalTestConfig;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMFormElement;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.exception.ParseException;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a>
 * Date: 15.10.2021 Time: 11:10
 */
@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
class FlowModelTest {
    private static final String PROCESS_NAME = "GROUPS_HO_S_WL1";
    private static final String PROCESS_NAME_2 = "GROUP_HO_S_WL1";
    private static final String RESOURCES_PATH = "/com/tesco/pma/flow/";
    private static final String FORM_1 = "forms/pm_o_1.form";
    private static final String FORM_2 = "pm_o_2.form";
    private static final String FILE_NAME_PM_V1 = "pm_v1.bpmn";
    private static final String FILE_NAME_PM_V2 = "pm_v2.bpmn";
    private static final String PROCESS_NAME_TYPE_1 = "type_1";
    private static final String PROCESS_NAME_TYPE_2 = "type_2";
    private static final String FORM_TYPE_1_OBJECTIVE = "forms/type_1_objective.form";
    private static final String FORM_TYPE_2_OBJECTIVE = "forms/type_2_objective.form";

    private final ResourceProvider resourceProvider = new FormsResourceProvider();

    private PMProcessModelParser parser;

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    private static class FormsResourceProvider implements ResourceProvider {
        @Override
        public InputStream read(String resourceName) throws IOException {
            return getClass().getResourceAsStream(RESOURCES_PATH + resourceName);
        }

        @Override
        public String resourceToString(final String resourceName) throws IOException {
            try (InputStream is = getClass().getResourceAsStream(RESOURCES_PATH + resourceName)) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        }
    }

    @BeforeEach
    void init() {
        parser = new PMProcessModelParser(resourceProvider, messageSourceAccessor);
    }

    @Test
    void readModel() throws IOException {
        checkModel(PROCESS_NAME, FILE_NAME_PM_V1, FORM_1, 2, 2);
    }

    @Test
    void readModel2() throws IOException {
        Assertions.assertThrows(ParseException.class, () -> checkModel(PROCESS_NAME_2, FILE_NAME_PM_V2, FORM_1, 3, 3));
    }

    @Test
    void readType1() throws IOException {
        checkModel(PROCESS_NAME_TYPE_1, PROCESS_NAME_TYPE_1 + ".bpmn", FORM_TYPE_1_OBJECTIVE, 5, 3);
    }

    @Test
    void readType2() throws IOException {
        checkModel(PROCESS_NAME_TYPE_2, PROCESS_NAME_TYPE_2 + ".bpmn", FORM_TYPE_2_OBJECTIVE, 5, 3);
    }

    @Test
    void getFormName() {
        assertEquals(FORM_1, parser.getFormName("camunda-forms:deployment:forms/pm_o_1.form"));
        assertEquals(FORM_2, parser.getFormName("camunda-forms:deployment:pm_o_2.form"));
    }

    @Test
    void unwrap() {
        Assertions.assertNull(PMProcessModelParser.unwrap(null));
        Assertions.assertNull(PMProcessModelParser.unwrap(""));
        Assertions.assertNull(PMProcessModelParser.unwrap(" "));

        var expected = "Set objectives";
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n objectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap(" Set  objectives "));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\nobjectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n\robjectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n\r\n\r objectives"));
    }

    @Test
    void defaultValue() {
        var expected = "Set objectives";
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue(null, expected));
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue("  ", expected));
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue("Set objectives ", ""));
    }

    private void checkModel(String processName, String processFileName, String formName, int tlSize, int reviewsCount) throws IOException {
        var model = getModel(processFileName);
        var metadata = parser.parse(model);
        assertNotNull(metadata);
        var cycle = metadata.getCycle();
        assertNotNull(cycle);

        assertEquals(tlSize, cycle.getTimelinePoints().size());

        var reviews = cycle.getTimelinePoints().stream()
                .filter(t -> PMElementType.REVIEW.name().equalsIgnoreCase(t.getType().name()))
                .map(t -> (PMReviewElement) t)
                .collect(Collectors.toList());
        assertEquals(reviewsCount, reviews.size());

        checkObjective(formName, reviews);
    }

    private void checkObjective(String formName, List<PMReviewElement> reviews) {
        Optional<PMReviewElement> oObjective = reviews.stream()
                .filter(r -> ReviewType.OBJECTIVE.name().equalsIgnoreCase(r.getReviewType().getCode()))
                .findFirst();
        assertTrue(oObjective.isPresent());
        PMFormElement form = oObjective.get().getForm();
        assertNotNull(form);
        assertTrue(form.getKey().contains(formName));
    }

    private BpmnModelInstance getModel(String processFileName) throws IOException {
        return Bpmn.readModelFromStream(resourceProvider.read(processFileName));
    }
}
