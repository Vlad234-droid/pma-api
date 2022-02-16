package com.tesco.pma.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public interface BatchService {
    <T> int executeDBOperationInBatch(Collection<T> origItemsList, ToIntFunction<List<T>> functionForApply);

    <T> Collection<List<T>> batchifyCollection(Collection<T> originalList);

    <T, P> Collection<List<Map.Entry<T, P>>> batchifyMap(Map<T, P> original);
}
