package com.benjamerc.spring_security_course.domain.dto.token;

import com.benjamerc.spring_security_course.domain.entity.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenWithRaw {

    private RefreshToken refreshToken;

    private String rawToken;
}
