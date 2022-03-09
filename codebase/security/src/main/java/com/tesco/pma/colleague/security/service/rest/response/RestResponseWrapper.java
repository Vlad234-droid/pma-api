package com.tesco.pma.colleague.security.service.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.error.ApiError;
import com.tesco.pma.rest.RestResponse;
import lombok.Value;

import java.util.Collection;

@Value
public class RestResponseWrapper<T> {

    RestResponse<T> original;
    int nextPage;

    @JsonValue
    public ExtendedRestResponse<T> toJson() {
        return new ExtendedRestResponse<>(
                original.isSuccess(),
                original.getData(),
                original.getErrors(),
                nextPage > 0 ? String.valueOf(nextPage) : null);
    }

    @Value
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private static class ExtendedRestResponse<T> {
        boolean success;
        T data;
        Collection<ApiError> errors;
        String nextPage;
    }

}
