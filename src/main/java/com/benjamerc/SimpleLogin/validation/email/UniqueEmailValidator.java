package com.benjamerc.SimpleLogin.validation.email;

import com.benjamerc.SimpleLogin.service.AuthService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final AuthService authService;

    @Autowired
    public UniqueEmailValidator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (email == null || email.isBlank()) {
            return true;
        }

        return authService.isEmailAvailable(email);
    }
}
