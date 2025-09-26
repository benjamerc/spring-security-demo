package com.benjamerc.spring_security_course.users.dto.response;

import com.benjamerc.spring_security_course.security.core.Role;

public record AdminUserSummaryResponse(

        Long id,

        String username,

        String name,

        Role role
) {}
