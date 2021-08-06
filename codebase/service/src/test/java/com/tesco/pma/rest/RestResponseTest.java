package com.tesco.pma.rest;

import com.tesco.pma.error.ApiError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RestResponseTest {
    private static final ApiError API_ERROR_1 = ApiError.builder().code("API_ERROR_1").build();
    private static final ApiError API_ERROR_2 = ApiError.builder().code("API_ERROR_2").build();
    private static final String TEST_DATA = "test data";

    @Test
    void success() {
        RestResponse response = RestResponse.success();

        assertThat(response)
                .returns(true, from(RestResponse::isSuccess))
                .returns(null, from(RestResponse::getData))
                .returns(null, from(RestResponse::getErrors));
    }

    @Test
    void successWithData() {
        RestResponse<String> response = RestResponse.success(TEST_DATA);

        assertThat(response)
                .returns(true, from(RestResponse::isSuccess))
                .returns(TEST_DATA, from(RestResponse::getData))
                .returns(null, from(RestResponse::getErrors));
    }

    @Test
    void successWithDataFailed() {
        assertThatIllegalArgumentException().isThrownBy(() -> RestResponse.success(null))
                .withMessage("data can't be null");
    }

    @Test
    void failWithSingleErrorSucceeded() {
        RestResponse response = RestResponse.fail(API_ERROR_1);

        assertThat(response)
                .returns(false, from(RestResponse::isSuccess))
                .returns(null, from(RestResponse::getData))
                .extracting(RestResponse::getErrors).asList().containsExactly(API_ERROR_1);
    }

    @Test
    void failWithSingleErrorFailed() {
        ApiError error = null;

        assertThatIllegalArgumentException().isThrownBy(() -> RestResponse.fail(error))
                .withMessage("error can't be null");
    }

    @Test
    void failWithErrorsSucceeded() {
        RestResponse response = RestResponse.fail(List.of(API_ERROR_1, API_ERROR_2));

        assertThat(response)
                .returns(false, from(RestResponse::isSuccess))
                .returns(null, from(RestResponse::getData))
                .extracting(RestResponse::getErrors).asList().containsExactly(API_ERROR_1, API_ERROR_2);
    }

    @Test
    void failWithErrorsFailed() {
        Collection<ApiError> nullErrors = null;
        Collection<ApiError> emptyErrors = Collections.emptyList();

        assertThatIllegalArgumentException().isThrownBy(() -> RestResponse.fail(nullErrors))
                .withMessage("errors can't be empty"); //NOPMD

        assertThatIllegalArgumentException().isThrownBy(() -> RestResponse.fail(emptyErrors))
                .withMessage("errors can't be empty"); //NOPMD
    }

    @Test
    void failWithDataAndErrorsSucceeded() {
        RestResponse<String> response = RestResponse.fail(TEST_DATA, List.of(API_ERROR_1, API_ERROR_2));

        assertThat(response)
                .returns(false, from(RestResponse::isSuccess))
                .returns(TEST_DATA, from(RestResponse::getData))
                .extracting(RestResponse::getErrors).asList().containsExactly(API_ERROR_1, API_ERROR_2);
    }

    @ParameterizedTest
    @MethodSource("dataFailWithDataAndErrorsFailed")
    void failWithDataAndErrorsFailed(Object data, Collection<ApiError> errors,
                                     Class<? extends Throwable> exceptionType, String message) {
        assertThatThrownBy(() -> RestResponse.fail(data, errors))
                .isInstanceOf(exceptionType)
                .hasMessage(message);
    }

    static Stream<Arguments> dataFailWithDataAndErrorsFailed() {
        return Stream.of(
                arguments(null, null, IllegalArgumentException.class, "data can't be null"),
                arguments(null, List.of(API_ERROR_1), IllegalArgumentException.class, "data can't be null"),
                arguments(TEST_DATA, null, IllegalArgumentException.class, "errors can't be empty"),
                arguments(TEST_DATA, Collections.emptyList(), IllegalArgumentException.class, "errors can't be empty")
        );
    }
}