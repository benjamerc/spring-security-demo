package com.benjamerc.spring_security_course.authentication.controller;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
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

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private IntegrationTestHelper helper;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();
    }

    // -----------------------
    // Helper methods
    // -----------------------
    private UserTokens createAndAuthenticateUser() {

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        return helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );
    }

    private ResponseEntity<AuthAuthenticateResponse> postRefresh(String refreshToken, String accessToken) {

        AuthRefreshTokenRequest request = AuthTestDataProvider.authRefreshTokenRequest(refreshToken);
        HttpEntity<AuthRefreshTokenRequest> entity = new HttpEntity<>(request, helper.authorizedHeaders(accessToken));

        return restTemplate.exchange("/api/auth/refresh", HttpMethod.POST, entity, AuthAuthenticateResponse.class);
    }

    private ResponseEntity<Void> postLogout(String refreshToken, String accessToken) {

        AuthRefreshTokenRequest request = AuthTestDataProvider.authRefreshTokenRequest(refreshToken);
        HttpEntity<AuthRefreshTokenRequest> entity = new HttpEntity<>(request, helper.authorizedHeaders(accessToken));

        return restTemplate.exchange("/api/auth/logout", HttpMethod.POST, entity, Void.class);
    }

    // -----------------------
    // Register tests
    // -----------------------
    @Test
    void shouldReturn201AndRegisterUserWithoutAuthentication() {

        AuthRegisterRequest request = AuthTestDataProvider.authRegisterRequest(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD
        );

        ResponseEntity<AuthRegisterResponse> response =
                restTemplate.postForEntity("/api/auth/register", request, AuthRegisterResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull()
                .extracting(AuthRegisterResponse::username, AuthRegisterResponse::name)
                .containsExactly(request.username(), request.name());
    }

    @Test
    void shouldReturn400WhenRegisterUserCalledWithExistentUsername() {

        User user = helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER);

        AuthRegisterRequest request = AuthTestDataProvider.authRegisterRequest(
                user.getUsername(),
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD);

        ResponseEntity<AuthRegisterResponse> response =
                restTemplate.postForEntity("/api/auth/register", request, AuthRegisterResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400WhenRegisterUserCalledWithInvalidDto() {

        AuthRegisterRequest request =
                AuthTestDataProvider.authRegisterRequest(null, "", "");

        ResponseEntity<AuthRegisterResponse> response =
                restTemplate.postForEntity("/api/auth/register", request, AuthRegisterResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -----------------------
    // Authenticate tests
    // -----------------------
    @Test
    void shouldReturn200AndAuthenticateUserWithoutAuthentication() {

        createAndAuthenticateUser();

        AuthAuthenticateRequest request = AuthTestDataProvider.authAuthenticateRequest();

        ResponseEntity<AuthAuthenticateResponse> response =
                restTemplate.postForEntity("/api/auth/authenticate", request, AuthAuthenticateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void shouldReturn401WhenAuthenticateUserCalledWithIncorrectCredentials() {

        createAndAuthenticateUser();

        AuthAuthenticateRequest request =
                AuthTestDataProvider.authAuthenticateRequest(UserTestDataProvider.USER_USERNAME, "wrongPassword");

        ResponseEntity<AuthAuthenticateResponse> response =
                restTemplate.postForEntity("/api/auth/authenticate", request, AuthAuthenticateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn400WhenAuthenticateUserCalledWithInvalidDto() {

        createAndAuthenticateUser();

        AuthAuthenticateRequest request =
                AuthTestDataProvider.authAuthenticateRequest("", null);

        ResponseEntity<AuthAuthenticateResponse> response =
                restTemplate.postForEntity("/api/auth/authenticate", request, AuthAuthenticateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -----------------------
    // Refresh token tests
    // -----------------------
    @Test
    void shouldReturn200AndRefreshTokenWithoutAuthentication() {

        UserTokens tokens = createAndAuthenticateUser();

        ResponseEntity<AuthAuthenticateResponse> response = postRefresh(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void shouldReturn401WhenRefreshTokenCalledWithRevokedToken() {

        UserTokens tokens = createAndAuthenticateUser();

        tokens.refreshToken().setRevoked(true);
        refreshTokenRepository.save(tokens.refreshToken());

        ResponseEntity<AuthAuthenticateResponse> response = postRefresh(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenRefreshTokenCalledWithExpiredToken() {

        UserTokens tokens = createAndAuthenticateUser();

        tokens.refreshToken().setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));
        refreshTokenRepository.save(tokens.refreshToken());

        ResponseEntity<AuthAuthenticateResponse> response = postRefresh(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn404WhenRefreshTokenCalledWithNonexistentToken() {

        UserTokens tokens = createAndAuthenticateUser();

        ResponseEntity<AuthAuthenticateResponse> response = postRefresh(AuthTestDataProvider.REFRESH_TOKEN_STRING, tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -----------------------
    // Logout tests
    // -----------------------
    @Test
    void shouldReturn204AndLogoutActualSessionWithoutAuthentication() {

        UserTokens tokens = createAndAuthenticateUser();

        ResponseEntity<Void> response = postLogout(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenLogoutActualSessionCalledWhitRevokedToken() {

        UserTokens tokens = createAndAuthenticateUser();

        tokens.refreshToken().setRevoked(true);
        refreshTokenRepository.save(tokens.refreshToken());

        ResponseEntity<Void> response = postLogout(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenLogoutActualSessionCalledWhitExpiredToken() {

        UserTokens tokens = createAndAuthenticateUser();

        tokens.refreshToken().setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));
        refreshTokenRepository.save(tokens.refreshToken());

        ResponseEntity<Void> response = postLogout(tokens.rawRefreshToken(), tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn404WhenLogoutActualSessionCalledWhitNonexistentToken() {

        UserTokens tokens = createAndAuthenticateUser();

        ResponseEntity<Void> response = postLogout(AuthTestDataProvider.REFRESH_TOKEN_STRING, tokens.accessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
