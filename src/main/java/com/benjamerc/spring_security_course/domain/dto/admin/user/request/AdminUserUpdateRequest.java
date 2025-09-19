package com.benjamerc.spring_security_course.domain.dto.admin.user.request;

import com.benjamerc.spring_security_course.security.Role;

public record AdminUserUpdateRequest(

        String username,

        String name,

        Role role
) {}
