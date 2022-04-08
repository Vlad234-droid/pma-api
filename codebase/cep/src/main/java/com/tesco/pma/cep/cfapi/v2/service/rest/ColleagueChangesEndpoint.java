package com.tesco.pma.cep.cfapi.v2.service.rest;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.cfapi.v2.service.ColleagueChangesService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.logging.TraceId;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.tesco.pma.cep.cfapi.v2.AppMessageCode.CEP_EVENT_OCCURRED;
import static com.tesco.pma.cep.cfapi.v2.exception.ErrorCodes.CEP_EVENT_PAYLOAD_ERROR;

@RestController
@RequestMapping(path = "/colleagues")
@Validated
@Slf4j
public class ColleagueChangesEndpoint {

    private final ColleagueChangesService colleagueChangesService;
    private final Executor executor;
    private final NamedMessageSourceAccessor messages;

    private static final String COLLEAGUE_CHANGE_EVENT_PAYLOAD_PARAMETER_NAME = "colleagueChangeEventPayload";

    public ColleagueChangesEndpoint(ColleagueChangesService colleagueChangesService,
                                    @Qualifier("processingTaskExecutor") Executor executor,
                                    NamedMessageSourceAccessor messages) {
        this.colleagueChangesService = colleagueChangesService;
        this.executor = executor;
        this.messages = messages;
    }

    /**
     * Consuming colleague changes events
     *
     * @param colleagueChangeEventPayload - payload request
     * @return a RestResponse with status
     */
    // @Hidden - uncomment after integration
    @Operation(summary = "Consuming events", description = "Consuming colleague changes events", tags = {"cep"})
    @ApiResponse(responseCode = HttpStatusCodes.TOO_MANY_REQUESTS, description = "Too Many Requests", content = @Content)
    @PostMapping("/events")
    @PreAuthorize("isAdmin() or authentication.name == @cepProperties.subject")
    public ResponseEntity<Void> processColleagueChangeEvent(@RequestHeader HttpHeaders headers,
                                                      @RequestBody ColleagueChangeEventPayload colleagueChangeEventPayload) {

        if (Objects.isNull(colleagueChangeEventPayload) || Objects.isNull(colleagueChangeEventPayload.getEventType())) {
            log.warn(LogFormatter.formatMessage(messages, CEP_EVENT_PAYLOAD_ERROR,
                    Map.of(COLLEAGUE_CHANGE_EVENT_PAYLOAD_PARAMETER_NAME, colleagueChangeEventPayload)));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        var traceId = getTraceId(headers);

        logEvent(headers, colleagueChangeEventPayload);

        startProcessingColleagueChangeEvent(colleagueChangeEventPayload, traceId);

        return ResponseEntity.ok().build();

    }

    private void logEvent(HttpHeaders headers, ColleagueChangeEventPayload colleagueChangeEventPayload) {
        String headerMessage = "Header: {EventId: %s, FeedId: %s, TraceId: %s}";
        if (headers != null) {
            var eventId = headers.getFirst("EventId");
            var feedId = headers.getFirst("FeedId");
            var traceId = headers.getFirst("TraceId");
            headerMessage = String.format(headerMessage, eventId, feedId, traceId);
        }

        String payloadMessage = String.format("Payload: {%s}", colleagueChangeEventPayload);

        String message = String.format("{%s, %s}", headerMessage, payloadMessage);
        log.info(LogFormatter.formatMessage(CEP_EVENT_OCCURRED, message));
    }

    private TraceId getTraceId(HttpHeaders headers) {
        var traceId = TraceUtils.getTraceId();

        if (headers != null) {
            var traceIdHeader = headers.getFirst("TraceId");
            if (traceIdHeader != null) {
                traceId = new TraceId(traceIdHeader, null);
                TraceUtils.setTraceId(traceId);
            }
        }

        return traceId;
    }

    private void startProcessingColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                                     TraceId traceId) {

        executor.execute(() -> {
            TraceUtils.setTraceId(traceId);
            colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);
        });

    }

}
