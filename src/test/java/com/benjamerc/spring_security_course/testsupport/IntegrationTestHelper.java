package com.benjamerc.spring_security_course.testsupport;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.authentication.repository.RefreshTokenRepository;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.security.core.TokenUtils;
import com.benjamerc.spring_security_course.testsupport.dto.UserTokens;
import com.benjamerc.spring_security_course.users.UserTestFactory;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class IntegrationTestHelper {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public User createUser(String username, String name, String password, Role role) {

        return userRepository.findByUsername(username)
                .orElseGet(() -> {

                    User user = UserTestFactory.defineUser(username, name, password, role, passwordEncoder);

                    return userRepository.save(user);
                });
    }

    public HttpHeaders authorizedHeaders(String token) {

        HttpHeaders headers = new HttpHeaders();

        if (token != null) headers.setBearerAuth(token);

        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    public UserTokens authenticateAndGetTokens(String username, String password) {

        AuthAuthenticateRequest request = AuthTestDataProvider.authAuthenticateRequest(username, password);

        ResponseEntity<AuthAuthenticateResponse> response =
                restTemplate.postForEntity("/api/auth/authenticate", request, AuthAuthenticateResponse.class);

        return getTokens(response);
    }

    private UserTokens getTokens(ResponseEntity<AuthAuthenticateResponse> response) {

        assertThat(response.getBody())
                .as("Expected a non-null response body from /api/auth/authenticate")
                .isNotNull();

        String rawToken = response.getBody().refreshToken();
        assertThat(rawToken)
                .as("Expected a non-null refresh token in the authentication response")
                .isNotNull();

        String hashedToken = TokenUtils.hashSHA256(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException(
                        "Refresh token not found in DB for hashed token: " + hashedToken));

        return new UserTokens(
                response.getBody().accessToken(),
                refreshToken,
                rawToken
        );
    }
}
