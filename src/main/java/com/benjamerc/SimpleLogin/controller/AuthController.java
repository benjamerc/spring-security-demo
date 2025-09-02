package com.benjamerc.SimpleLogin.controller;

import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserLoginRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserRegisterRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserLoginResponse;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserRegisterResponse;
import com.benjamerc.SimpleLogin.domain.entity.UserEntity;
import com.benjamerc.SimpleLogin.mapper.AuthMapper;
import com.benjamerc.SimpleLogin.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AuthMapper authMapper;

    @Autowired
    public AuthController(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserEntity userEntity = authMapper.registerRequestToEntity(request);
        UserEntity savedUser = authService.register(userEntity);
        UserRegisterResponse response = authMapper.entityToRegisterResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserEntity userEntity = authMapper.loginRequestToEntity(request);
        UserEntity loggedUser = authService.login(userEntity);
        UserLoginResponse response = authMapper.entityToLoginResponse(loggedUser);

        return ResponseEntity.ok(response);
    }
}
