package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.shared.dto.pagination.CustomPage;
import com.benjamerc.spring_security_course.testsupport.IntegrationTestHelper;
import com.benjamerc.spring_security_course.testsupport.dto.UserTokens;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserResponse;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdminUserControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestHelper helper;

    @Autowired
    private UserRepository userRepository;

    private String userToken;
    private String adminToken;

    private Long userId;

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

        userId = userRepository.findByUsername(UserTestDataProvider.USER_USERNAME)
                .orElseThrow().getId();
    }

    @Test
    void shouldReturn200AndAllUsersWhenAdminIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<CustomPage<AdminUserSummaryResponse>> response = restTemplate.exchange(
                "/api/admin/users",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<CustomPage<AdminUserSummaryResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getContent()).isNotEmpty();
        assertThat(response.getBody().getPageNumber()).isEqualTo(0);
        assertThat(response.getBody().getPageSize()).isEqualTo(20);

        assertThat(response.getBody().getTotalElements()).isEqualTo(response.getBody().getContent().size());
        assertThat(response.getBody().getTotalPages()).isEqualTo(1);
        assertThat(response.getBody().isLastPage()).isTrue();
    }

    @Test
    void shouldReturn401WhenGetAllUsersCalledWithNotAuthenticatedAdmin() {

        ResponseEntity<CustomPage<AdminUserSummaryResponse>> response = restTemplate.exchange(
                "/api/admin/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CustomPage<AdminUserSummaryResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenGetAllUsersCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

        ResponseEntity<CustomPage<AdminUserSummaryResponse>> response = restTemplate.exchange(
                "/api/admin/users",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<CustomPage<AdminUserSummaryResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn200AndGetUserByIdWhenAdminIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.GET,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userId);
        assertThat(response.getBody().username()).isEqualTo(UserTestDataProvider.USER_USERNAME);
    }

    @Test
    void shouldReturn401WhenGetUserByIdCalledWithNotAuthenticatedAdmin() {

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.GET,
                null,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenGetUserByIdCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.GET,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn404WhenGetUserByIdCalledWithNonexistentId() {

        long nonExistentId = 99L;

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + nonExistentId,
                HttpMethod.GET,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn200AndPartialUpdateUserWhenAdminIsAuthenticated() {

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        HttpEntity<AdminUserUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(adminToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.PATCH,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(AdminUserResponse::id, AdminUserResponse::username, AdminUserResponse::name)
                .containsExactly(userId, updateRequest.username(), UserTestDataProvider.USER_NAME);
    }

    @Test
    void shouldReturn401WhenPartialUpdateCalledWithNotAuthenticatedAdmin() {

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        HttpEntity<AdminUserUpdateRequest> entity = new HttpEntity<>(updateRequest, null);

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.PATCH,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenPartialUpdateCalledWithIncorrectRole() {

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        HttpEntity<AdminUserUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(userToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.PATCH,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn404WhenPartialUpdateCalledWithNonexistentId() {

        long nonExistentId= 99L;

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        HttpEntity<AdminUserUpdateRequest> entity = new HttpEntity<>(updateRequest, helper.authorizedHeaders(adminToken));

        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                "/api/admin/users/" + nonExistentId,
                HttpMethod.PATCH,
                entity,
                AdminUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn204AndDeleteUserAccountWhenAdminIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenDeleteUserAccountCalledWithNotAuthenticatedAdmin() {

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenDeleteUserAccountCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn404WhenDeleteUserAccountCalledWithNonexistentId() {

        long nonExistentId = 99L;

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + nonExistentId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn204AndForceLogoutAllUserSessionsWhenAdminIsAuthenticated() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId + "/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WhenForceLogoutAllUserSessionsCalledWithNotAuthenticatedAdmin() {

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId + "/logout-all",
                HttpMethod.POST,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenForceLogoutAllUserSessionsCalledWithIncorrectRole() {

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(userToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + userId + "/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn404WhenForceLogoutAllUserSessionsCalledWithNonexistentId() {

        long nonExistentId = 99L;

        HttpEntity<Void> entity = new HttpEntity<>(helper.authorizedHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/users/" + nonExistentId + "/logout-all",
                HttpMethod.POST,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
