package com.tesco.pma.util;

import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.file.api.File;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestDataUtil {

    public static final UUID USER_UUID = UUID.fromString("af1cb887-2c46-4715-a11a-509e8df49635");
    public static final UUID CYCLE_UUID = UUID.fromString("aa1cb887-2c46-4715-a11a-509e8df49635");
    public static final UUID TEMPLATE_UUID = UUID.fromString("bb1cb887-2c46-4715-a11a-509e8df49635");
    public static final String CODE = "CODE";
    public static final String CYCLE_NAME = "Cycle name";
    public static final String TEST_FILE_NAME = "test_file_name";

    public static PMCycle buildCycle() {
        var cycle = new PMCycle();
        cycle.setUuid(CYCLE_UUID);
        cycle.setName(CYCLE_NAME);

        return cycle;
    }

    public static PMCycle buildCycleWithMetadata() {
        var cycle = buildCycle();
        var metadata = new PMCycleMetadata();
        var element = new PMCycleElement();
        element.setCode(CODE);
        metadata.setCycle(element);
        cycle.setMetadata(metadata);
        File template = new File();
        template.setUuid(TEMPLATE_UUID);
        template.setFileName(TEST_FILE_NAME);
        cycle.setTemplate(template);
        return cycle;
    }


}
