package com.tesco.pma.validation;

import com.tesco.pma.security.UserRoleNames;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint to check allowed User roles.
 */
@AllowedValues(UserRoleNames.SUBSIDIARY_MANAGER)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.PARAMETER, ElementType.TYPE_PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {}
)

public @interface UserRolesAllowed {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
