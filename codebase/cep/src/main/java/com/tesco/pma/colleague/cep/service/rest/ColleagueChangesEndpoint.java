package com.tesco.pma.colleague.cep.service.rest;

import com.tesco.pma.colleague.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.colleague.cep.service.ColleagueChangesService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.service.cep.EventRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.Executor;

@RestController
@RequestMapping(path = "/colleagues/cep")
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
     * @param eventRequest - event request
     */
    // @Hidden - uncomment after integration
    @Operation(summary = "Consuming events", description = "Consuming colleague changes events", tags = {"cep"})
    @ApiResponse(responseCode = HttpStatusCodes.TOO_MANY_REQUESTS, description = "Too Many Requests", content = @Content)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/events")
    //TODO:: implement authorization for service-service communication - check access to endpoint by identity subject and/or client_id:
    //for example: @PreAuthorize("isAllowed('REQUESTS_START_PROCESS')")
    //@PreAuthorize("authentication.name == @cepProperties.subject")
    public void processColleagueChangeEvent(@RequestBody EventRequest<ColleagueChangeEventPayload> eventRequest) {
        if (Objects.isNull(eventRequest.getPayload()) || Objects.isNull(eventRequest.getPayload().eventType())) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.EVENT_PAYLOAD_ERROR, "Invalid payload was received from CEP"));
            return;
        }

        processColleagueChangeEvent(eventRequest.getPayload());

    }

    private void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        var traceId = TraceUtils.getTraceId();

        executor.execute(() -> {
            TraceUtils.setTraceId(traceId);
            colleagueChangesService.processColleagueChangeEvent(colleagueChangeEventPayload);
        });

    }

    @Hidden
    @PostMapping("/events/local")
    @ResponseStatus(HttpStatus.CREATED)
    public void processColleagueChangeEventLocal(@RequestBody EventRequest<ColleagueChangeEventPayload> eventRequest) {
        processColleagueChangeEvent(eventRequest);
    }

}
