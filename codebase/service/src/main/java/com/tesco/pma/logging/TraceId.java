package com.tesco.pma.logging;

import lombok.Value;

@Value
public class TraceId {

    public static final String TRACE_ID_HEADER = "TraceId";

    String value;
    TraceId parent;
}
