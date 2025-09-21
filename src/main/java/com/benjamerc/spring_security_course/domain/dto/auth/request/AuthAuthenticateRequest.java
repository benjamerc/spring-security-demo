package com.benjamerc.spring_security_course.domain.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthAuthenticateRequest(

        @Email(message = "Username must be a valid email address")
        @Size(max = 100, message = "Username cannot exceed 100 characters")
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        @NotBlank(message = "Password cannot be blank")
        String password
) {}
