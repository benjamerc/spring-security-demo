package com.benjamerc.spring_security_course.shared.validation;

import com.benjamerc.spring_security_course.shared.validation.validator.UniqueValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValueValidator.class)
public @interface UniqueValue {

    String message() default "Value already exists";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
