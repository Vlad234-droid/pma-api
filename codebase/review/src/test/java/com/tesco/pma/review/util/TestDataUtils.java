package com.tesco.pma.review.util;

import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.file.api.File;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.TimelinePoint;
import lombok.experimental.UtilityClass;
import org.jeasy.random.EasyRandom;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class TestDataUtils {

    public static final UUID TL_POINT_UUID = UUID.fromString("982b2124-b712-429f-aad1-656cbcadc1b5");

    private static final EasyRandom RANDOM = new EasyRandom();

    public static List<File> files(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(TestDataUtils::file)
                .collect(toList());
    }

    private static File file(int index) {
        File file = RANDOM.nextObject(File.class);
        file.setFileName("fileName" + index);
        file.setPath("path" + index);
        return file;
    }

    public static TimelinePoint buildTimelinePoint() {
        return TimelinePoint.builder()
                .uuid(TL_POINT_UUID)
                .build();
    }

    public static OrgObjective buildOrgObjective(String title) {
        final var randomUUID = UUID.randomUUID();
        return OrgObjective.builder()
                .uuid(randomUUID)
                .number(1)
                .status(OrgObjectiveStatus.DRAFT)
                .title(title)
                .version(1)
                .build();
    }

}
