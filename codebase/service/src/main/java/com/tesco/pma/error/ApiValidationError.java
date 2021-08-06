package com.tesco.pma.error;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Implementation of {@link ErrorDetail} for Validation exceptions/errors
 * eg {@link javax.validation.ConstraintViolationException}, {@link MethodArgumentNotValidException}
 * * <pre>
 * * Example:
 * * {
 * * "error": {
 * *     "code": "CONSTRAINT_VIOLATION",
 * *     "message": "Constraint violation",
 * *     "details": [
 * *          {
 * *            "message": "must be greater than 0",
 * *            "field": "update.product.product_price",
 * *            "rejected_value": "-23.0"
 * *           }
 * *         ]
 * *    },
 * * "success": false
 * * }
 *  * </pre>
 */

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class ApiValidationError extends ErrorDetail {
    private String field;
    private String rejectedValue;
}
