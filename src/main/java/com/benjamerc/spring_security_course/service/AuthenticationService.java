package com.benjamerc.spring_security_course.service;

import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthRegisterResponse;

import java.util.UUID;

public interface AuthenticationService {

    AuthRegisterResponse register(AuthRegisterRequest request);

    AuthAuthenticateResponse login(AuthAuthenticateRequest request);

    AuthAuthenticateResponse refreshToken(AuthRefreshTokenRequest request);

    void logout(AuthRefreshTokenRequest request);
}
