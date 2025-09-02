package com.benjamerc.SimpleLogin.service;

import com.benjamerc.SimpleLogin.domain.entity.UserEntity;

public interface AuthService {

    UserEntity register(UserEntity user);

    UserEntity login(UserEntity user);

    boolean isEmailAvailable(String email);
}
