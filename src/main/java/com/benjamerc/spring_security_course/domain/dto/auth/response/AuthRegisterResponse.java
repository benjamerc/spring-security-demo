package com.benjamerc.spring_security_course.domain.dto.auth.response;

import com.benjamerc.spring_security_course.security.Role;

public record AuthRegisterResponse(

        String username,

        String name,

        Role role
) {}

