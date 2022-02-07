package com.tesco.pma.cep.v2.service.rest;

import com.tesco.pma.cep.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.v2.service.ColleagueChangesService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.Executor;

import static com.tesco.pma.cep.v2.exception.ErrorCodes.EVENT_FEED_ID_ERROR;

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
     */
    // @Hidden - uncomment after integration
    @Operation(summary = "Consuming events", description = "Consuming colleague changes events", tags = {"cep"})
    @ApiResponse(responseCode = HttpStatusCodes.TOO_MANY_REQUESTS, description = "Too Many Requests", content = @Content)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/events")
    @PreAuthorize("isAdmin() or authentication.name == @cepProperties.subject")
    public void processColleagueChangeEvent(@RequestHeader("FeedId") String feedId,
                                            @RequestBody ColleagueChangeEventPayload colleagueChangeEventPayload) {
        if (feedId == null) {
            log.warn(LogFormatter.formatMessage(EVENT_FEED_ID_ERROR, "Invalid feedId header was received from CEP"));
            log.debug(String.format("Tried to process a event request %s", colleagueChangeEventPayload));
            return;
        }

        if (Objects.isNull(colleagueChangeEventPayload) || Objects.isNull(colleagueChangeEventPayload.getEventType())) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.EVENT_PAYLOAD_ERROR, "Invalid payload was received from CEP"));
            log.debug(String.format("Tried to process a event request %s", colleagueChangeEventPayload));
            return;
        }

        startProcessingColleagueChangeEvent(feedId, colleagueChangeEventPayload);

    }

    private void startProcessingColleagueChangeEvent(String feedId,
                                                     ColleagueChangeEventPayload colleagueChangeEventPayload) {
        var traceId = TraceUtils.getTraceId();

        executor.execute(() -> {
            TraceUtils.setTraceId(traceId);
            colleagueChangesService.processColleagueChangeEvent(feedId, colleagueChangeEventPayload);
        });

    }

}
