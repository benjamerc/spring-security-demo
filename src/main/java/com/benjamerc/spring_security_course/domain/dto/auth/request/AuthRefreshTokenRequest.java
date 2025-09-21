package com.benjamerc.spring_security_course.domain.dto.auth.request;

import java.util.UUID;

public record AuthRefreshTokenRequest(

        UUID token
) {}
