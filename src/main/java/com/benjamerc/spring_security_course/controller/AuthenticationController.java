package com.benjamerc.spring_security_course.controller;

import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(@RequestBody @Valid AuthRegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthAuthenticateResponse> login(@RequestBody @Valid AuthAuthenticateRequest request) {

        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthAuthenticateResponse> refreshToken(@RequestBody @Valid AuthRefreshTokenRequest request) {

        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestBody @Valid AuthRefreshTokenRequest request) {

        authenticationService.logout(request);

        return ResponseEntity.noContent().build();
    }
}
