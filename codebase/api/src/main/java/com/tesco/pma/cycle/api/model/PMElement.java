package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.DictionaryItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.10.2021 Time: 21:13
 */
@Data
@NoArgsConstructor
public class PMElement implements DictionaryItem<String>, Serializable {
    private static final long serialVersionUID = -4451575369183429990L;

    public static final String PM_PREFIX = "pm_";
    public static final String PM_TYPE = PM_PREFIX + "type";

    private String id;
    private String code;
    private String description;
    private PMElementType type;
    private Map<String, String> properties = new LinkedHashMap<>();

    protected PMElement(String id, String code, String description, PMElementType type) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.type = type;
    }

    protected static List<String> getPropertyNames(Class<? extends PMElement> cls, String matches) {
        return Arrays.stream(cls.getDeclaredFields())
                .map(Field::getName).filter(name -> name.toLowerCase().matches(matches)).collect(Collectors.toList());
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMElement.class, PM_PREFIX + "(?!prefix)([\\w]+)$");
    }
}
