package com.tesco.pma.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

/**
 * The annotated {@code String} must be one of the values.
 *
 * <p>Accepts {@code String}. {@code null} elements are considered valid.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = AllowedValues.AllowedValuesValidator.class
)
public @interface AllowedValues {
    String message() default "{com.tesco.pma.validation.constraints.AllowedValues.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] value();

    class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {
        private Set<String> allowedValues;

        @Override
        public void initialize(AllowedValues constraintAnnotation) {
            allowedValues = Set.of(constraintAnnotation.value());
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return true;
            }
            return allowedValues.contains(value);
        }
    }
}
