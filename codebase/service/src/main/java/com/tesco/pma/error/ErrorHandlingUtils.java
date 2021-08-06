package com.tesco.pma.error;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@UtilityClass
public final class ErrorHandlingUtils {

    /**
     *  Method builds ResponseEntity object
     *
     * @param headers HttpHeaders
     * @param status HttpStatus
     * @param body response body
     * @return the ResponseEntity object
     */
    static ResponseEntity<Object> createResponseEntity(HttpHeaders headers, HttpStatus status, String body) {

        return ResponseEntity
                .status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
