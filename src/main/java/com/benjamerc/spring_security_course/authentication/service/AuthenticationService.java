package com.benjamerc.spring_security_course.authentication.service;

import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;

public interface AuthenticationService {

    AuthRegisterResponse register(AuthRegisterRequest request);

    AuthAuthenticateResponse login(AuthAuthenticateRequest request);

    AuthAuthenticateResponse refreshToken(AuthRefreshTokenRequest request);

    void logout(AuthRefreshTokenRequest request);
}
