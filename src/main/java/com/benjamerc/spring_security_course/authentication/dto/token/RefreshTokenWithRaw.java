package com.benjamerc.spring_security_course.authentication.dto.token;

import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenWithRaw {

    private RefreshToken refreshToken;

    private String rawToken;
}
