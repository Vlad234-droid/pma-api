package com.tesco.pma.config.service;

import com.tesco.pma.config.parser.model.FieldSet;
import com.tesco.pma.config.parser.model.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ColleagueEntityValidator {

    Set<String> validateColleague(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .map(v -> v.get("colleague_uuid"))
                .filter(v -> !isPKUuidValid(v))
                .map(Value::getFormatted)
                .collect(Collectors.toSet());
    }

    private static boolean isPKUuidValid(Value value) {
        if (value == null || StringUtils.isEmpty(value.getFormatted())) {
            return false;
        }
        try {
            UUID.fromString(value.getFormatted());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
