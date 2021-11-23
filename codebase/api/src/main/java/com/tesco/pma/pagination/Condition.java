package com.tesco.pma.pagination;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Used to describe next filer conditions:
 * "_publicationState": "preview",
 * "published_at_null": "false"
 *
 * <p/>List of available suffixes could be found
 * @see <a href="https://strapi.io/documentation/developer-docs/latest/developer-resources/content-api/content-api.html#filters">here</a>
 */
@Data
public class Condition {
    private final String property;
    private final Operand operand;
    private final Object value;

    public static Condition build(String property, Object value) {
        var operand = Operand.get(property);
        return new Condition(operand.removeOperand(property), operand, value);
    }

    @Getter
    public enum Operand {
        EQUALS("_eq"),
        NOT_EQUALS("_ne"),
        CONTAINS("_contains"),
        NOT_CONTAINS("_ncontains"),
        IN("_in"),
        NOT_IN("_nin"),
        LESS_THAN("_lt"),
        LESS_THAN_EQUALS("_lte"),
        GREATER_THAN("_gt"),
        GREATER_THAN_EQUALS("_gte"),
        LIKE("_like"),
        NULL("_null");

        private final String suffix;

        Operand(String suffix) {
            this.suffix = suffix;
        }

        public String removeOperand(String property) {
            return StringUtils.removeEnd(property, suffix);
        }

        public static Operand get(String property) {
            var split = property.split("_");
            var op = "_" + split[split.length - 1];
            return Arrays.stream(values())
                    .filter(v -> v.suffix.equals(op))
                    .findFirst()
                    .orElse(EQUALS);
        }
    }
}
