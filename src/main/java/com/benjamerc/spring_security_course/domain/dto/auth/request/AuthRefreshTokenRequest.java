package com.benjamerc.spring_security_course.domain.dto.auth.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuthRefreshTokenRequest(

        @NotNull(message = "Refresh token must be provided")
        UUID token
) {}
