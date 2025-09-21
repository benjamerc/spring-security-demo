package com.benjamerc.spring_security_course.domain.dto.auth.response;

import java.util.UUID;

public record AuthAuthenticateResponse(

        String accessToken,

        UUID refreshToken
) {}
