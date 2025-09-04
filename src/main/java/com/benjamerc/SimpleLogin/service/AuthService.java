package com.benjamerc.SimpleLogin.service;

import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserLoginRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserLoginResponse;
import com.benjamerc.SimpleLogin.domain.entity.UserEntity;

public interface AuthService {

    UserEntity register(UserEntity user);

    UserLoginResponse login(UserLoginRequest request);

    boolean isEmailAvailable(String email);
}
