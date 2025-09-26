package com.benjamerc.spring_security_course.users.dto.request;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.shared.validation.NotAdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(

        @Email(message = "Username must be a valid email address")
        @Size(max = 100, message = "Username cannot exceed 100 characters")
        String username,

        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotAdminRole
        Role role
) {}
