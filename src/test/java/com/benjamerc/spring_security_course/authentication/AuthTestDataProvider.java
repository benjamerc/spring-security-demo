package com.benjamerc.spring_security_course.authentication;

import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.users.model.User;

import java.time.Instant;
import java.util.UUID;

public class AuthTestDataProvider {

    public static RefreshToken refreshToken(User user) {

        return RefreshToken.builder()
                .token("a976d35e-43f3-4f94-8ae3-9f84652dcc51")
                .expiryDate(Instant.parse("2050-12-31T23:59:59Z"))
                .session(UUID.fromString("1e584051-cc15-42ac-a60e-668cd004a25d"))
                .user(user)
                .build();
    }

    public static RefreshToken refreshToken(User user, Long id) {

        return RefreshToken.builder()
                .id(id)
                .token("a976d35e-43f3-4f94-8ae3-9f84652dcc51")
                .expiryDate(Instant.parse("2050-12-31T23:59:59Z"))
                .session(UUID.fromString("1e584051-cc15-42ac-a60e-668cd004a25d"))
                .user(user)
                .build();
    }
}
