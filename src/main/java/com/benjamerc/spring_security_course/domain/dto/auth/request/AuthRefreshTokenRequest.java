package com.benjamerc.spring_security_course.domain.dto.auth.request;

import jakarta.validation.constraints.NotNull;

public record AuthRefreshTokenRequest(

        @NotNull(message = "Refresh token must be provided")
        String token
) {}
