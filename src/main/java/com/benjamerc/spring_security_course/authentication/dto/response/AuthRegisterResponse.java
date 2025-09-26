package com.benjamerc.spring_security_course.authentication.dto.response;

import com.benjamerc.spring_security_course.security.core.Role;

public record AuthRegisterResponse(

        String username,

        String name,

        Role role
) {}

