package com.benjamerc.spring_security_course.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(

        @Email(message = "Username must be a valid email address")
        @Size(max = 100, message = "Username cannot exceed 100 characters")
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        @NotBlank(message = "Password cannot be blank")
        String password
) {}