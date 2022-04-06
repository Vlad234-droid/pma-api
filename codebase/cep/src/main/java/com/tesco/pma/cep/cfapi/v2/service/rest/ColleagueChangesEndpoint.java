package com.tesco.pma.cep.cfapi.v2.service.rest;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.cfapi.v2.service.ColleagueChangesService;
import com.tesco.pma.exception.ErrorCodes;
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

import java.util.Objects;
import java.util.concurrent.Executor;

import static com.tesco.pma.cep.cfapi.v2.exception.DebugCodes.CEP_EVENT_HEADERS;
import static com.tesco.pma.cep.cfapi.v2.exception.DebugCodes.CEP_EVENT_PAYLOAD;

@RestController
@RequestMapping(path = "/colleagues")
@Validated
@Slf4j
public class ColleagueChangesEndpoint {

    private final ColleagueChangesService colleagueChangesService;
    private final Executor executor;

    public ColleagueChangesEndpoint(ColleagueChangesService colleagueChangesService,
                                    @Qualifier("processingTaskExecutor") Executor executor) {
        this.colleagueChangesService = colleagueChangesService;
        this.executor = executor;
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

        var traceId = TraceUtils.getTraceId();

        if (log.isDebugEnabled() && headers != null) {
            var eventIdHeader = headers.getFirst("EventId");
            var feedIdHeader = headers.getFirst("FeedId");
            var traceIdHeader = headers.getFirst("TraceId");
            if (traceIdHeader != null) {
                traceId = new TraceId(traceIdHeader, null);
                TraceUtils.setTraceId(traceId);
            }
            String message = String.format("Tried to process a event request for eventId = %s, feedId = %s, traceId = %s",
                    eventIdHeader, feedIdHeader, traceIdHeader);
            log.debug(LogFormatter.formatMessage(CEP_EVENT_HEADERS, message));

        }

        if (Objects.isNull(colleagueChangeEventPayload) || Objects.isNull(colleagueChangeEventPayload.getEventType())) {
            String message = String.format("Invalid payload was received from CEP = %s", colleagueChangeEventPayload);
            log.warn(LogFormatter.formatMessage(ErrorCodes.EVENT_PAYLOAD_ERROR, message));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        if (log.isDebugEnabled()) {
            String message = String.format("Tried to process an event request %s", colleagueChangeEventPayload);
            log.debug(LogFormatter.formatMessage(CEP_EVENT_PAYLOAD, message));
        }

        startProcessingColleagueChangeEvent(colleagueChangeEventPayload, traceId);

        return ResponseEntity.ok().build();

    }

    private void startProcessingColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                                     TraceId traceId) {

        executor.execute(() -> {
            TraceUtils.setTraceId(traceId);
            colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);
        });

    }

}
