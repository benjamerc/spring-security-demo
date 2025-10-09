package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.testsupport.IntegrationTestHelper;
import com.benjamerc.spring_security_course.testsupport.dto.UserTokens;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestHelper helper;

    @Autowired
    private UserRepository userRepository;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        helper.createUser(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.USER_NAME,
                UserTestDataProvider.PASSWORD,
                Role.USER
        );

        helper.createUser(
                UserTestDataProvider.ADMIN_USERNAME,
                UserTestDataProvider.ADMIN_NAME,
                UserTestDataProvider.PASSWORD,
                Role.ADMIN
        );

        UserTokens userTokens = helper.authenticateAndGetTokens(
                UserTestDataProvider.USER_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        UserTokens adminTokens  = helper.authenticateAndGetTokens(
                UserTestDataProvider.ADMIN_USERNAME,
                UserTestDataProvider.PASSWORD
        );

        userToken = userTokens.accessToken();
        adminToken = adminTokens.accessToken();
    }

    @Test
    void shouldReturn200AndUserProfileWhenUserIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

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

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

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

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(userToken));

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

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(null));

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

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(adminToken));

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

        HttpEntity<UserPartialUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(userToken));

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

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

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

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

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

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

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

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/user/me/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
