package com.tesco.pma.logging.filter;

import com.tesco.pma.logging.TraceId;
import com.tesco.pma.logging.TraceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;

@Component
public class TracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var traceIdHeader = request.getHeader(TRACE_ID_HEADER);
        var traceId = StringUtils.isNotEmpty(traceIdHeader) ? new TraceId(traceIdHeader, null) : TraceUtils.generateRandom();
        TraceUtils.setTraceId(traceId);
        response.addHeader(TRACE_ID_HEADER, traceId.getValue());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceUtils.clear();
        }
    }
}
