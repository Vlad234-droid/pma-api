package com.tesco.pma.logging;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TraceUtils {
    private static final ThreadLocal<TraceId> TRACE_ID_HOLDER = new ThreadLocal<>(); //NOSONAR remove() is used in filter

    /**
     * Gets trace id object, generates new if object is not present in thread local
     *
     * @return trace id object
     */
    public TraceId getTraceId() {
        var traceId = TRACE_ID_HOLDER.get();
        if (null == traceId) {
            traceId = generateRandom();
            setTraceId(traceId);
        }
        return traceId;
    }

    /**
     * Sets current trace id object as parent and generates new object to wrap parent as chold
     *
     * @return trace id object
     */
    public TraceId toParent() {
        var parent = TRACE_ID_HOLDER.get();
        var childTraceId = new TraceId(UUID.randomUUID().toString(), parent);
        setTraceId(childTraceId);
        return childTraceId;
    }

    /**
     * Sets trace id object to thread local
     *
     * @param traceId - trace id object
     */
    public void setTraceId(TraceId traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    /**
     * Generates new random trace id object
     *
     * @return trace id object
     */
    public TraceId generateRandom() {
        return new TraceId(UUID.randomUUID().toString(), null);
    }

    /**
     * Removes current trace id from context
     */
    public void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
