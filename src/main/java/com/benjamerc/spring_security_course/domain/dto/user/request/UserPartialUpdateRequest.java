package com.benjamerc.spring_security_course.domain.dto.user.request;

public record UserPartialUpdateRequest(

        String username,

        String name
) {}
