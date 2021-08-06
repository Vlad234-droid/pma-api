package com.tesco.pma.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.error.ApiError;
import lombok.Value;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

/**
 * General API response model.
 * In case no data should be returned use raw (not parameterized) type.
 *
 * @param <T> - type of Data returned by API call.
 */
@Value
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class RestResponse<T> {
    boolean success;
    T data;
    Collection<ApiError> errors;

    /**
     * Constructor.
     *
     * @param success succeeded
     * @param data    response data, could be null.
     * @param errors  response errors, could be null.
     */
    private RestResponse(boolean success, T data, Collection<ApiError> errors) {
        this.success = success;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Simple success api response.
     * Without data and errors.
     *
     * @return success api response.
     */
    public static RestResponse<Void> success() {
        return new RestResponse<>(true, null, null);
    }

    /**
     * Success api response with data.
     *
     * @param <T>  - actual data type.
     * @param data response data, can't be null.
     * @return success api response with data.
     * @throws IllegalArgumentException if the data is null.
     */
    public static <T> RestResponse<T> success(T data) {
        Assert.notNull(data, "data can't be null");
        return new RestResponse<>(true, data, null);
    }

    /**
     * Failed api response with data and errors.
     *
     * @param <T>    - actual data type.
     * @param data   response data, can't be null.
     * @param errors collection of api errors
     * @return failed api response with data and errors.
     * @throws IllegalArgumentException if the data is null, errors is null or empty.
     */
    public static <T> RestResponse<T> fail(T data, Collection<ApiError> errors) {
        Assert.notNull(data, "data can't be null");
        Assert.notEmpty(errors, "errors can't be empty");
        return new RestResponse<>(false, data, errors);
    }

    /**
     * Failed api response with errors.
     *
     * @param errors collection of api errors
     * @return failed api response with errors.
     * @throws IllegalArgumentException if the errors is null or empty.
     */
    public static RestResponse<Void> fail(Collection<ApiError> errors) {
        Assert.notEmpty(errors, "errors can't be empty");
        return new RestResponse<>(false, null, errors);
    }

    /**
     * Failed api response with single error.
     *
     * @param error api error, can't be null.
     * @return failed api response with single error.
     * @throws IllegalArgumentException if the error is null.
     */
    public static RestResponse<Void> fail(ApiError error) {
        Assert.notNull(error, "error can't be null");
        return fail(List.of(error));
    }

}