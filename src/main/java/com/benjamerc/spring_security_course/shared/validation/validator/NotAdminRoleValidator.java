package com.benjamerc.spring_security_course.shared.validation.validator;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.shared.validation.NotAdminRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotAdminRoleValidator implements ConstraintValidator<NotAdminRole, Role> {

    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        return role != Role.ADMIN;
    }
}
