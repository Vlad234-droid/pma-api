package com.tesco.pma.cep.cfapi.v2.service.rest;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.cfapi.v2.service.ColleagueChangesService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
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

        if (headers != null) {
            var eventId = headers.getFirst("EventId");
            var feedId = headers.getFirst("FeedId");
            var traceId = headers.getFirst("TraceId");
            log.debug(String.format("Tried to process a event request for eventId = %s, feedId = %s, traceId = %s",
                    eventId, feedId, traceId));
        }

        if (Objects.isNull(colleagueChangeEventPayload) || Objects.isNull(colleagueChangeEventPayload.getEventType())) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.EVENT_PAYLOAD_ERROR, "Invalid payload was received from CEP"));
            log.debug(String.format("Tried to process a event request %s", colleagueChangeEventPayload));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        startProcessingColleagueChangeEvent(colleagueChangeEventPayload);

        return ResponseEntity.ok().build();

    }

    private void startProcessingColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        var traceId = TraceUtils.getTraceId();

        executor.execute(() -> {
            TraceUtils.setTraceId(traceId);
            colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);
        });

    }

}
