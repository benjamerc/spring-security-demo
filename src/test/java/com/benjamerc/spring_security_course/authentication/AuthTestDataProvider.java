package com.benjamerc.spring_security_course.authentication;

import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.token.RefreshTokenWithRaw;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;

import java.time.Instant;
import java.util.UUID;

public class AuthTestDataProvider {

    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN_STRING = "a976d35e-43f3-4f94-8ae3-9f84652dcc51";
    public static final UUID REFRESH_TOKEN_SESSION = UUID.fromString("1e584051-cc15-42ac-a60e-668cd004a25d");
    public static final long REFRESH_TOKEN_EXPIRATION = 2592000000L;

    public static RefreshToken refreshToken(User user) {

        return RefreshToken.builder()
                .token(REFRESH_TOKEN_STRING)
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
                .session(REFRESH_TOKEN_SESSION)
                .user(user)
                .build();
    }

    public static RefreshToken refreshToken(User user, Long id) {

        return RefreshToken.builder()
                .id(id)
                .token(REFRESH_TOKEN_STRING)
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
                .session(REFRESH_TOKEN_SESSION)
                .user(user)
                .build();
    }

    public static RefreshTokenWithRaw refreshTokenWithRaw(RefreshToken refreshToken, String rawToken) {

        return new RefreshTokenWithRaw(refreshToken, rawToken);
    }

    public static AuthRegisterRequest authRegisterRequest() {

        return new AuthRegisterRequest(UserTestDataProvider.USER_USERNAME, UserTestDataProvider.USER_NAME, UserTestDataProvider.PASSWORD);
    }

    public static AuthRegisterRequest authRegisterRequest(String username, String name, String password) {

        return new AuthRegisterRequest(username, name, password);
    }

    public static AuthAuthenticateRequest authAuthenticateRequest() {

        return new AuthAuthenticateRequest(UserTestDataProvider.USER_USERNAME, UserTestDataProvider.PASSWORD);
    }

    public static AuthAuthenticateRequest authAuthenticateRequest(String username, String password) {

        return new AuthAuthenticateRequest(username, password);
    }

    public static AuthRefreshTokenRequest authRefreshTokenRequest() {

        return new AuthRefreshTokenRequest(REFRESH_TOKEN_STRING);
    }

    public static AuthRefreshTokenRequest authRefreshTokenRequest(String token) {

        return new AuthRefreshTokenRequest(token);
    }
}
