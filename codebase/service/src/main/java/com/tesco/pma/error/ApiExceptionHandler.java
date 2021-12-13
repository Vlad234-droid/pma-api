package com.tesco.pma.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.CodeAware;
import com.tesco.pma.api.TargetAware;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.DataUploadException;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.ExternalSystemException;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.exception.LimitExceededException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.ReviewCreationException;
import com.tesco.pma.exception.ReviewDeletionException;
import com.tesco.pma.exception.ReviewUpdateException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.tesco.pma.exception.ErrorCodes.ACCESS_DENIED;
import static com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION;
import static com.tesco.pma.exception.ErrorCodes.ER_CODE_UNEXPECTED_EXCEPTION;
import static com.tesco.pma.exception.ErrorCodes.LIMIT_EXCEEDED;
import static com.tesco.pma.exception.ErrorCodes.MESSAGE_NOT_READABLE_EXCEPTION;
import static com.tesco.pma.exception.ErrorCodes.METHOD_ARGUMENT_TYPE_MISMATCH;
import static com.tesco.pma.exception.ErrorCodes.MISSING_SERVLET_REQUEST_PART_EXCEPTION;

/**
 * Custom controllerAdvice for handling application errors/exceptions and transform them into
 * app specific 'failed' {@link RestResponse}
 */
@ControllerAdvice
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String SW_INTERNAL_SERVER_ERROR_MESSAGE = "Unexpected internal service error";
    private static final String BODY = "body";
    private static final String FILE_NAME = "fileName";
    private static final String MEDIA_TYPE = "mediaType";
    private static final String PARAMETER = "parameter";
    private static final String VALUE = "value";
    private static final String TYPE = "type";

    private final AccessDeniedHandler accessDeniedHandler = new BearerTokenAccessDeniedHandler();

    private final ObjectMapper om;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Value("${tesco.application.external-endpoints.cep.retry-timeout:30}")
    private int retryTimeout;

    /**
     * Handle {@link AccessDeniedException}.
     * Process with {@link BearerTokenAccessDeniedHandler}.
     * With standard api body {@link RestResponse}.
     *
     * @param ex       exception
     * @param request  request
     * @param response response
     * @return failed {@link RestResponse}
     * @throws ServletException
     * @throws IOException
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    protected RestResponse<Void> handleAccessDeniedException(AccessDeniedException ex,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) throws ServletException, IOException {
        final var message = messageSourceAccessor.getMessage(ACCESS_DENIED);
        logger.error(LogFormatter.formatMessage(ACCESS_DENIED, message), ex);

        accessDeniedHandler.handle(request, response, ex);
        return RestResponse.fail(ApiError.builder()
                .code(ACCESS_DENIED.getCode())
                .message(message)
                .build());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    @Hidden
    protected RestResponse<Void> handleAlreadyExistsException(AlreadyExistsException ex) {
        logger.error(LogFormatter.formatMessage(ex, ex.getMessage()), ex);
        return RestResponse.fail(ApiError.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(value = {
            InvalidParameterException.class,
            InvalidPayloadException.class,
            DataUploadException.class,
            ReviewCreationException.class,
            ReviewUpdateException.class,
            ReviewDeletionException.class})
    protected ResponseEntity<Object> handleBadRequestAPI(AbstractApiRuntimeException ex, WebRequest request) {
        logger.error(LogFormatter.formatMessage(ex, "Bad request error has been occurred"), ex);
        return createResponse(ex, null, HttpStatus.BAD_REQUEST);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {

        var message = "Request parse error: " + ex.getMessage();
        logger.error(LogFormatter.formatMessage(messageSourceAccessor, MESSAGE_NOT_READABLE_EXCEPTION, message), ex);

        var error = ApiError.builder()
                .code(MESSAGE_NOT_READABLE_EXCEPTION.getCode())
                .message(messageSourceAccessor.getMessage(MESSAGE_NOT_READABLE_EXCEPTION))
                .build();

        if (ex.getRootCause() != null) {
            error.addDetails(ApiValidationError.builder()
                    .code(ex.getRootCause().getClass().getName())
                    .message(ex.getRootCause().getMessage())
                    .field(getFieldName(ex))
                    .build());
        }

        return createResponse(RestResponse.fail(error), null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleInternal(Exception ex, WebRequest request) {
        return createInternalErrorResponse(ex, null);
    }

    @ExceptionHandler(LimitExceededException.class)
    protected ResponseEntity<Object> handleLimitExceededException(LimitExceededException ex) {
        logger.warn(LogFormatter.formatMessage(messageSourceAccessor, LIMIT_EXCEEDED));
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(retryTimeout))
                .build();
    }

    @ExceptionHandler(value = {ExternalSystemException.class, RegistrationException.class})
    protected ResponseEntity<Object> handleExternalSystemException(AbstractApiRuntimeException ex) {
        logger.error(LogFormatter.formatMessage(ex, ex.getMessage()), ex);

        var error = ApiError.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return createResponse(RestResponse.fail(error), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = "Required request part is not present: " + ex.getMessage();
        logger.error(LogFormatter.formatMessage(messageSourceAccessor, MISSING_SERVLET_REQUEST_PART_EXCEPTION, message), ex);
        return createResponse(new InvalidPayloadException(MISSING_SERVLET_REQUEST_PART_EXCEPTION.name(),
                        messageSourceAccessor.getMessage(MISSING_SERVLET_REQUEST_PART_EXCEPTION), BODY),
                null, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConstraintViolationException.
     *
     * @param ex      ConstraintViolationException reports the result of constraint violations.
     * @param request WebRequest
     * @return the ResponseEntity object
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ResponseEntity<Object> handleViolationException(ConstraintViolationException ex, WebRequest request) {
        String message = messageSourceAccessor.getMessage(CONSTRAINT_VIOLATION);
        logger.error(LogFormatter.formatMessage(CONSTRAINT_VIOLATION, message), ex);

        var error = ApiError.builder()
                .code(CONSTRAINT_VIOLATION.getCode())
                .message(message)
                .build();

        ex.getConstraintViolations().stream()
                .map(cv -> (ErrorDetail) ApiValidationError.builder()
                        .message(cv.getMessage())
                        .rejectedValue(String.valueOf(cv.getInvalidValue()))
                        .field(String.valueOf(cv.getPropertyPath()))
                        .build())
                .forEach(error::addDetails);

        return createResponse(RestResponse.fail(error), null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NotFoundException.class)
    protected ResponseEntity<Object> handleDataNotFoundException(NotFoundException ex, WebRequest request) {
        logger.error(LogFormatter.formatMessage(ex, ex.getMessage()), ex);

        var error = ApiError.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return createResponse(RestResponse.fail(error), null, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = DatabaseConstraintViolationException.class)
    protected ResponseEntity<Object> handleDatabaseConstraintViolationException(DatabaseConstraintViolationException ex,
                                                                                WebRequest request) {
        logger.error(LogFormatter.formatMessage(ex, ex.getMessage()), ex);

        ApiError error = ApiError.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return createResponse(RestResponse.fail(error), null, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ResponseEntity object
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        var builder = new StringBuilder()
                .append(messageSourceAccessor.getMessage(ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                        Collections.singletonMap(MEDIA_TYPE, ex.getContentType())))
                .append(' ');
        ex.getSupportedMediaTypes().forEach(type -> builder.append(type).append(", "));

        RestResponse<?> errorResponse = RestResponse.fail(
                ApiError.builder()
                        .code(ErrorCodes.UNSUPPORTED_MEDIA_TYPE.name())
                        .message(builder.substring(0, builder.length() - 2))
                        .build()
        );

        return createResponse(errorResponse, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ResponseEntity object
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {

        String message = messageSourceAccessor.getMessage(ErrorCodes.METHOD_ARGUMENT_NOT_VALID);
        logger.error(LogFormatter.formatMessage(ErrorCodes.METHOD_ARGUMENT_NOT_VALID, message));

        var error = ApiError.builder()
                .code(ErrorCodes.METHOD_ARGUMENT_NOT_VALID.name())
                .message(message)
                .build();


        var bindingResult = ex.getBindingResult();
        if (bindingResult.hasFieldErrors()) {
            bindingResult.getFieldErrors().stream()
                    .map(fieldError -> (ErrorDetail) ApiValidationError.builder()
                            .rejectedValue(String.valueOf(fieldError.getRejectedValue()))
                            .field(fieldError.getField())
                            .message(fieldError.toString())
                            .build())
                    .forEach(error::addDetails);
        }

        if (bindingResult.hasGlobalErrors()) {
            bindingResult.getGlobalErrors().stream()
                    .map(globalError -> (ErrorDetail) ApiValidationError.builder()
                            .code(globalError.getCode())
                            .message(globalError.toString())
                            .build())
                    .forEach(error::addDetails);
        }


        return createResponse(RestResponse.fail(error), null, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentTypeMismatchException.
     *
     * @param ex      the MethodArgumentNotValidException raised while resolving a controller method argument
     * @param request WebRequest
     * @return the ResponseEntity object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message = messageSourceAccessor.getMessage(ErrorCodes.METHOD_ARGUMENT_TYPE_MISMATCH,
                Map.of(PARAMETER, ex.getName(),
                        VALUE, Objects.requireNonNullElse(ex.getValue(), StringUtils.EMPTY),
                        TYPE, ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "undefined"));

        logger.error(LogFormatter.formatMessage(METHOD_ARGUMENT_TYPE_MISMATCH, message), ex);

        var error = ApiError.builder()
                .code(ErrorCodes.METHOD_ARGUMENT_TYPE_MISMATCH.name())
                .message(message)
                .build();

        return createResponse(RestResponse.fail(error), null, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> createInternalErrorResponse(Exception ex, HttpHeaders headers) {
        logger.error(LogFormatter.formatMessage(messageSourceAccessor, ER_CODE_UNEXPECTED_EXCEPTION, SW_INTERNAL_SERVER_ERROR_MESSAGE), ex);
        return ErrorHandlingUtils.createResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR, toJson(ex));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {

        return createInternalErrorResponse(ex, headers);
    }


    private ResponseEntity<Object> createResponse(AbstractApiRuntimeException ex, HttpHeaders headers, HttpStatus status) {
        try {
            return ErrorHandlingUtils.createResponseEntity(headers, status, om.writeValueAsString(toErrorResponse(ex)));
        } catch (JsonProcessingException jpe) {
            return createInternalErrorResponse(ex, headers);
        }
    }

    private ResponseEntity<Object> createResponse(RestResponse<?> errorResponse, HttpHeaders headers, HttpStatus httpStatus) {
        try {
            return ErrorHandlingUtils.createResponseEntity(null, httpStatus, om.writeValueAsString(errorResponse));
        } catch (JsonProcessingException e) {
            return createInternalErrorResponse(e, headers);
        }
    }

    private String toJson(Exception ex) {
        StringBuilder sb = new StringBuilder("{\"success\":false,\"errors\":[{")
                .append("\"code\":\"").append(ex instanceof CodeAware ? ((CodeAware) ex).getCode() : ER_CODE_UNEXPECTED_EXCEPTION.getCode())
                .append("\",")
                .append("\"message\":\"").append(wrapJson(ex.getMessage())).append("\",")
                .append("\"target\":\"").append(ex instanceof TargetAware ? ((TargetAware) ex).getTarget() : "")
                .append('"');
        if (ex.getCause() != null) {
            toJsonInternal(sb, ex.getCause());
        }
        sb.append("}]}");
        return sb.toString();
    }

    private void toJsonInternal(StringBuilder sb, Throwable tr) {
        sb.append(",\"innerError\":{")
                .append("\"code\":\"").append(tr instanceof CodeAware ? ((CodeAware) tr).getCode() : tr.getClass().getName()).append("\",")
                .append("\"message\":\"").append(wrapJson(tr.getMessage())).append('"');
        if (tr.getCause() != null) {
            toJsonInternal(sb, tr.getCause());
        }
        sb.append('}');
    }

    private String wrapJson(final String message) {
        return message == null || message.isEmpty() ? "" : message.replace("\"", "\\\"");
    }

    private RestResponse<?> toErrorResponse(AbstractApiRuntimeException ex) {
        return RestResponse.fail(ApiError.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .target(ex.getTarget())
                .build());
    }

    private String getFieldName(HttpMessageNotReadableException e) {
        var fieldName = "";
        var cause = e.getCause();

        if (cause instanceof JsonMappingException) {
            var jme = (JsonMappingException) cause;
            if (!CollectionUtils.isEmpty(jme.getPath())) {
                fieldName = jme.getPath().get(jme.getPath().size() - 1).getFieldName();
            }
        }

        return fieldName;
    }
}
