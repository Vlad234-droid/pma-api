package com.tesco.pma.service.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 08.10.2021 Time: 11:50
 */
@Service
@Slf4j
public class ProcessMetadataService {
    Map<String, SortedMap<String, String>> timelines = new HashMap<>();

    public Map<String, SortedMap<String, String>> getTimelines() {
        return timelines;
    }

    public SortedMap<String, String> getTimeline(String processName) {
        return timelines.get(processName);
    }

    public void addTimeline(String processName, String point, String name) {
        SortedMap<String, String> timeline = timelines.get(processName);
        if (timeline == null) {
            timeline = new TreeMap<>();
        }
        timeline.put(point, name);
    }
}
