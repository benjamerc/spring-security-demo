package com.benjamerc.spring_security_course.shared.validation;

import com.benjamerc.spring_security_course.shared.validation.validator.NotAdminRoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotAdminRoleValidator.class)
public @interface NotAdminRole {

    String message() default "Cannot assign ADMIN role";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
