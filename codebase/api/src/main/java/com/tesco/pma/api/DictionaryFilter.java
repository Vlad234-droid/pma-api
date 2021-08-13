package com.tesco.pma.api;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// TODO revise class usage
public class DictionaryFilter<T extends DictionaryItem<? extends Serializable>> {

    final boolean include;
    final Set<T> items = new HashSet<>();

    protected DictionaryFilter(boolean include, Collection<T> items) {
        this.include = include;
        if (items != null && !items.isEmpty()) {
            this.items.addAll(items);
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DictionaryFilter)) {
            return false;
        }

        final DictionaryFilter<?> that = (DictionaryFilter<?>) obj;

        if (include != that.include) {
            return false;
        }
        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        int result = include ? 1 : 0;
        result = 31 * result + items.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DictionaryFilter{" + "include=" + include + ", items=" + items + '}';
    }

    @SafeVarargs
    public static <T extends DictionaryItem<? extends Serializable>> DictionaryFilter<T> includeFilter(T... items) {
        return includeFilter(convert(items));
    }

    public static <T extends DictionaryItem<? extends Serializable>> DictionaryFilter<T> includeFilter(Collection<T> items) {
        return new DictionaryFilter<>(true, items);
    }

    @SafeVarargs
    public static <T extends DictionaryItem<? extends Serializable>> DictionaryFilter<T> excludeFilter(T... items) {
        return excludeFilter(convert(items));
    }

    public static <T extends DictionaryItem<? extends Serializable>> DictionaryFilter<T> excludeFilter(Collection<T> items) {
        return new DictionaryFilter<>(false, items);
    }

    public static <T extends DictionaryItem<? extends Serializable>> DictionaryFilter<T> emptyFilter() {
        return excludeFilter(Collections.emptyList());
    }

    @SafeVarargs
    protected static <T extends DictionaryItem<? extends Serializable>> Set<T> convert(T... items) {
        return items != null ? new HashSet<T>(Arrays.asList(items)) : Collections.emptySet();
    }

}
