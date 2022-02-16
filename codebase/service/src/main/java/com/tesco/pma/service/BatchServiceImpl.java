package com.tesco.pma.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchServiceImpl implements BatchService {

    private static final String BATCH_SIZE_KEY = "batchSize";
    private static final String DEFAULT_BATCH_SIZE_VALUE = "100";

    private final int batchSize;

    public BatchServiceImpl(@Qualifier("mybatisSessionConfigurationProperties") Properties properties) {
        this.batchSize = Integer.parseInt(properties.getProperty(BATCH_SIZE_KEY, DEFAULT_BATCH_SIZE_VALUE));
    }

    /**
     * Perform action in batches
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> int executeDBOperationInBatch(Collection<T> origItemsList, ToIntFunction<List<T>> functionForApply) {
        if (origItemsList.isEmpty()) {
            return 0;
        }
        Collection<List<T>> batchifyItemsList = batchifyCollection(origItemsList);

        return batchifyItemsList.stream()
                .mapToInt(items -> {
                    var count = functionForApply.applyAsInt(items);

                    if (count != items.size()) {
                        throw new DataIntegrityViolationException("List of items were not stored");
                    }
                    return count;
                })
                .sum();
    }

    @Override
    public <T> Collection<List<T>> batchifyCollection(Collection<T> originalList) {
        if (CollectionUtils.isEmpty(originalList)) {
            return Collections.emptyList();
        }
        final var counter = new AtomicInteger(0);

        return originalList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize)).values();
    }

    @Override
    public  <T, P> Collection<List<Map.Entry<T, P>>> batchifyMap(Map<T, P> original) {
        if (CollectionUtils.isEmpty(original)) {
            return Collections.emptyList();
        }

        final var counter = new AtomicInteger(0);

        return original.entrySet().stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize)).values();
    }
}
