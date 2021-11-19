package com.tesco.pma.api;

import com.tesco.pma.pagination.RequestQuery;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.pagination.Condition.Operand.NOT_IN;

/**
 * Converts from a filter of RequestQuery with IN, NOT_IN Operand to DictionaryFilter
 * @param <T> represents DictionaryItem implementation as Enum
 */
@Component
public class RequestQueryToDictionaryFilterConverter<T extends Enum & DictionaryItem<? extends Serializable>> {

    public DictionaryFilter<T> convert(RequestQuery requestQuery, boolean isIncludeOperand, String property, Class<T> enumType) {
        var condition = requestQuery.getFilters().stream()
                .filter(cond -> property.equalsIgnoreCase(cond.getProperty())
                        && isIncludeOperand ? IN == cond.getOperand() : NOT_IN == cond.getOperand())
                .findFirst();
        if (condition.isPresent()) {
            condition.get().getValue();
            var items = ((List<String>) condition.get().getValue()).stream()
                    .map(i -> (T)T.valueOf(enumType, i))
                    .collect(Collectors.toSet());
            return isIncludeOperand ? DictionaryFilter.includeFilter(items) : DictionaryFilter.excludeFilter(items);
        }

        return DictionaryFilter.emptyFilter();
    }
}