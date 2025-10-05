package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.UserTestFactory;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        User definedUser = UserTestFactory.defineUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER,
                passwordEncoder
        );

        User definedAdmin = UserTestFactory.defineUser(
                UserTestDataProvider.ADMIN_USERNAME,
                UserTestDataProvider.ADMIN_NAME,
                UserTestDataProvider.PASSWORD,
                Role.ADMIN,
                passwordEncoder
        );

        userRepository.saveAll(List.of(definedUser, definedAdmin));

        userToken = authenticateAndGetToken(definedUser.getUsername(), UserTestDataProvider.PASSWORD);
        adminToken = authenticateAndGetToken(definedAdmin.getUsername(), UserTestDataProvider.PASSWORD);
    }

    protected HttpHeaders authorizedHeaders(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    private String authenticateAndGetToken(String username, String password) {

        AuthAuthenticateRequest request = AuthTestDataProvider.authAuthenticateRequest(username, password);

        ResponseEntity<AuthAuthenticateResponse> response =
                restTemplate.postForEntity("/api/auth/authenticate", request, AuthAuthenticateResponse.class);

        assertThat(response.getBody()).isNotNull();

        return response.getBody().accessToken();
    }

    @Test
    void shouldReturn200AndUserProfileWhenUserIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(userToken));

        ResponseEntity<UserProfileResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.GET,
                entity,
                UserProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(UserProfileResponse::username, UserProfileResponse::name)
                .containsExactly(UserTestDataProvider.USER_USERNAME, UserTestDataProvider.USER_NAME);
    }

    @Test
    void shouldReturn401WhenUserProfileCalledWithNotAuthenticatedUser() {

        ResponseEntity<UserProfileResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.GET,
                null,
                UserProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenUserProfileCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(adminToken));

        ResponseEntity<UserProfileResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.GET,
                entity,
                UserProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn200AndUpdateUserProfileWhenUserIsAuthenticated() {

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, authorizedHeaders(userToken));

        ResponseEntity<UserPartialUpdateResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.PATCH,
                entity,
                UserPartialUpdateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(UserPartialUpdateResponse::username, UserPartialUpdateResponse::name)
                .containsExactly(UserTestDataProvider.USER_USERNAME, updateRequest.name());
    }

    @Test
    void shouldReturn401WhenUpdateUserProfileCalledWithNotAuthenticatedUser() {

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, authorizedHeaders(null));

        ResponseEntity<UserPartialUpdateResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.PATCH,
                entity,
                UserPartialUpdateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenUpdateUserProfileCalledWithIncorrectRole() {

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, authorizedHeaders(adminToken));

        ResponseEntity<UserPartialUpdateResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.PATCH,
                entity,
                UserPartialUpdateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn400WhenUpdateUserProfileCalledWithInvalidDto() {

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest(null,"");

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, authorizedHeaders(userToken));

        ResponseEntity<UserPartialUpdateResponse> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.PATCH,
                entity,
                UserPartialUpdateResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn204AndDeleteUserAccountWhenUserIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(userToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenDeleteUserAccountCalledWithNotAuthenticatedUser() {

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenDeleteUserAccountCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me",
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn204AndLogoutAllUserSessionsWhenUserIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(userToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenLogoutAllUserSessionsCalledWithNotAuthenticatedUser() {

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me/logout-all",
                HttpMethod.POST,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenLogoutAllUserSessionsCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
