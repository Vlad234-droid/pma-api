package com.tesco.pma.pagination;

import lombok.Data;

import java.util.Arrays;

/**
 * Used to describe next string format as Java object
 * "_sort": "created_at:desc",
 */
@Data
public class Sort {
    private final String field;
    private final SortOrder order;

    public static Sort build(final String sort) {
        var split = sort.split(":");
        return new Sort(split[0], split.length == 1 ? SortOrder.ASC : SortOrder.fromString(split[1]));
    }

    public enum SortOrder {
        ASC, DESC;

        public static SortOrder fromString(String param) {
            return Arrays.stream(values())
                    .filter(v -> v.name().equalsIgnoreCase(param))
                    .findFirst()
                    .orElse(ASC);
        }
    }
}
