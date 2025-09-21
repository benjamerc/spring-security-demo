package com.benjamerc.spring_security_course.validation.validator;

import com.benjamerc.spring_security_course.security.Role;
import com.benjamerc.spring_security_course.validation.NotAdminRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotAdminRoleValidator implements ConstraintValidator<NotAdminRole, Role> {

    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        return role != Role.ADMIN;
    }
}
