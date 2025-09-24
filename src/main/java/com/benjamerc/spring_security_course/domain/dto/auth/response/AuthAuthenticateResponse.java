package com.benjamerc.spring_security_course.domain.dto.auth.response;

public record AuthAuthenticateResponse(

        String accessToken,

        String refreshToken
) {}
