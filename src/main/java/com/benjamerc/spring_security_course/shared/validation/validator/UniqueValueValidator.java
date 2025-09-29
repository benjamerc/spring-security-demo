package com.benjamerc.spring_security_course.shared.validation.validator;

import com.benjamerc.spring_security_course.shared.validation.UniqueValue;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueValueValidator implements ConstraintValidator<UniqueValue, String> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {

        if (username == null || username.isBlank()) {
            return true;
        }

        return !userRepository.existsByUsername(username);
    }
}
