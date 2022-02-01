package com.tesco.pma.review.util;

import com.tesco.pma.file.api.File;
import lombok.experimental.UtilityClass;
import org.jeasy.random.EasyRandom;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class TestDataUtils {

    private static final EasyRandom RANDOM = new EasyRandom();

    public static List<File> files(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(TestDataUtils::file)
                .collect(toList());
    }

    static private File file(int index) {
        File file = RANDOM.nextObject(File.class);
        file.setFileName("fileName" + index);
        file.setPath("path" + index);
        return file;
    }

}
