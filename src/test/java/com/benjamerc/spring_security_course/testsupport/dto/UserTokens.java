package com.benjamerc.spring_security_course.testsupport.dto;

import com.benjamerc.spring_security_course.authentication.model.RefreshToken;

public record UserTokens(

        String accessToken,

        RefreshToken refreshToken,

        String rawRefreshToken
) {}
