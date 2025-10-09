package com.benjamerc.spring_security_course.authentication.controller;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.authentication.repository.RefreshTokenRepository;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.testsupport.IntegrationTestHelper;
import com.benjamerc.spring_security_course.testsupport.dto.UserTokens;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestHelper helper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();
    }

    @Test
    void shouldReturn201AndRegisterUserWithoutAuthentication() {

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(
                        UserTestDataProvider.USER_USERNAME,
                        UserTestDataProvider.USER_NAME,
                        UserTestDataProvider.PASSWORD
                );

        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(registerRequest);

        ResponseEntity<AuthRegisterResponse> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                entity,
                AuthRegisterResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(AuthRegisterResponse::username, AuthRegisterResponse::name)
                .containsExactly(registerRequest.username(), registerRequest.name());
    }

    @Test
    void shouldReturn400WhenRegisterUserCalledWithExistentUsername() {

        User existentUser = helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(
                        existentUser.getUsername(),
                        UserTestDataProvider.USER_NAME,
                        UserTestDataProvider.PASSWORD
                );

        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(registerRequest);

        ResponseEntity<AuthRegisterResponse> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                entity,
                AuthRegisterResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400WhenRegisterUserCalledWithInvalidDto() {

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(
                        null,
                        "",
                        ""
                );

        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(registerRequest);

        ResponseEntity<AuthRegisterResponse> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                entity,
                AuthRegisterResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn200AndAuthenticateUserWithoutAuthentication() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        AuthAuthenticateRequest authenticateRequest =
                AuthTestDataProvider.authAuthenticateRequest();

        HttpEntity<AuthAuthenticateRequest> entity = new HttpEntity<>(authenticateRequest);

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/authenticate",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void shouldReturn401WhenAuthenticateUserCalledWithIncorrectCredentials() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        AuthAuthenticateRequest authenticateRequest =
                AuthTestDataProvider.authAuthenticateRequest(UserTestDataProvider.USER_USERNAME, "incorrect password");

        HttpEntity<AuthAuthenticateRequest> entity = new HttpEntity<>(authenticateRequest);

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/authenticate",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn400WhenAuthenticateUserCalledWithInvalidDto() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        AuthAuthenticateRequest authenticateRequest =
                AuthTestDataProvider.authAuthenticateRequest("", null);

        HttpEntity<AuthAuthenticateRequest> entity = new HttpEntity<>(authenticateRequest);

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/authenticate",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn200AndRefreshTokenWhenUserIsAuthenticated() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        String accessToken = tokens.accessToken();
        String rawRefreshToken = tokens.rawRefreshToken();

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(rawRefreshToken);

        HttpEntity<AuthRefreshTokenRequest> entity = new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(accessToken));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/refresh",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void shouldReturn401WhenRefreshTokenCalledWithRevokedToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        String accessToken = tokens.accessToken();
        RefreshToken refreshToken = tokens.refreshToken();
        String rawRefreshToken = tokens.rawRefreshToken();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(rawRefreshToken);

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(accessToken));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/refresh",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenRefreshTokenCalledWithExpiredToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        String accessToken = tokens.accessToken();
        RefreshToken refreshToken = tokens.refreshToken();
        String rawRefreshToken = tokens.rawRefreshToken();

        refreshToken.setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));
        refreshTokenRepository.save(refreshToken);

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(rawRefreshToken);

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(accessToken));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/refresh",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn404WhenRefreshTokenCalledWithNonexistentToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        String accessToken = tokens.accessToken();

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(accessToken));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/refresh",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn204AndLogoutActualSessionWhenUserIsAuthenticated() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(tokens.rawRefreshToken());

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(tokens.accessToken()));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenLogoutActualSessionCalledWhitRevokedToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(tokens.rawRefreshToken());

        tokens.refreshToken().setRevoked(true);
        refreshTokenRepository.save(tokens.refreshToken());

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(tokens.accessToken()));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenLogoutActualSessionCalledWhitExpiredToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(tokens.rawRefreshToken());

        tokens.refreshToken().setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));
        refreshTokenRepository.save(tokens.refreshToken());

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(tokens.accessToken()));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn404WhenLogoutActualSessionCalledWhitNonexistentToken() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        UserTokens tokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        tokens.refreshToken().setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));
        refreshTokenRepository.save(tokens.refreshToken());

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        HttpEntity<AuthRefreshTokenRequest> entity =
                new HttpEntity<>(refreshTokenRequest, helper.authorizedHeaders(tokens.accessToken()));

        ResponseEntity<AuthAuthenticateResponse> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                entity,
                AuthAuthenticateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
