package com.benjamerc.spring_security_course.authentication.dto.response;

public record AuthAuthenticateResponse(

        String accessToken,

        String refreshToken
) {}
