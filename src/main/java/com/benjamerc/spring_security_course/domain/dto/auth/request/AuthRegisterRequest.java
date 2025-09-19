package com.benjamerc.spring_security_course.domain.dto.auth.request;

public record AuthRegisterRequest(

        String username,

        String name,

        String password
) {}