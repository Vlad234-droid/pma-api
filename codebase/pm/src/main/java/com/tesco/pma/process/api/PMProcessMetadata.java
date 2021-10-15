package com.tesco.pma.process.api;

import java.util.SortedMap;
import java.util.TreeMap;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 22:35
 */
@Getter
public class PMProcessMetadata {
    public static final String TIMELINE_POINT = "timelinePoint";
    public static final String TIMELINE_POINT_NAME = "timelinePointName";

    private SortedMap<String, String> timeline = new TreeMap<>();

    //todo start event properties
    //todo review notification properties

}
