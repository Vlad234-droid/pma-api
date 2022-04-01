package com.tesco.pma.dao.utils;

import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DAOUtils {

    // Replace single quote with two single quotes for PostgreSQL
    public static void resetFilters(RequestQuery requestQuery) {
        List<Condition> result = new ArrayList<>();
        for (Condition condition : requestQuery.getFilters()) {
            if (Condition.Operand.LIKE.equals(condition.getOperand())
                    && condition.getValue() instanceof String) {
                var value = ((String) condition.getValue()).replace("'", "''");
                result.add(new Condition(condition.getProperty(), condition.getOperand(), value));
            } else {
                result.add(condition);
            }
        }
        requestQuery.setFilters(result);
    }

}
