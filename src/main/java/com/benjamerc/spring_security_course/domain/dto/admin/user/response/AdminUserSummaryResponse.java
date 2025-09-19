package com.benjamerc.spring_security_course.domain.dto.admin.user.response;

import com.benjamerc.spring_security_course.security.Role;

public record AdminUserSummaryResponse(

        Long id,

        String username,

        String name,

        Role role
) {}
