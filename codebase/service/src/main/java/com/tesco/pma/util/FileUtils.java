package com.tesco.pma.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;

import java.time.Instant;

import static java.time.ZoneId.from;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;

@UtilityClass
public class FileUtils {
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";

    public String addTimeStampToFileName(String fileName, Instant momentInUTC) {
        var base = FilenameUtils.removeExtension(fileName);
        var extension = FilenameUtils.getExtension(fileName);

        var formattedCurrMomentInUTC = ofPattern(TIMESTAMP_FORMAT).withZone(from(UTC)).format(momentInUTC);
        return base + "-" + formattedCurrMomentInUTC + "." + extension;
    }
}