package com.benjamerc.spring_security_course.authentication.controller;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenExpiredException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenRevokedException;
import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
import com.benjamerc.spring_security_course.authentication.service.AuthenticationService;
import com.benjamerc.spring_security_course.security.core.AccessTokenService;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturn201AndRegisterUser() throws Exception {

        AuthRegisterRequest registerRequest = AuthTestDataProvider.authRegisterRequest();

        AuthRegisterResponse registerResponse =
                new AuthRegisterResponse(registerRequest.username(), registerRequest.name(), Role.USER);

        when(authenticationService.register(any(AuthRegisterRequest.class))).thenReturn(registerResponse);

        performPost("/api/auth/register", registerRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(registerResponse.username()))
                .andExpect(jsonPath("$.name").value(registerResponse.name()))
                .andExpect(jsonPath("$.role").value(registerResponse.role().toString()));

        verify(authenticationService).register(any(AuthRegisterRequest.class));
    }

    @Test
    void shouldThrow400WhenRegisterCalledWithExistentUsername() throws Exception {

        AuthRegisterRequest registerRequest = AuthTestDataProvider.authRegisterRequest();

        doThrow(new UsernameAlreadyExistsException("Username already exists"))
                .when(authenticationService).register(any(AuthRegisterRequest.class));

        performPost("/api/auth/register", registerRequest)
                .andExpect(status().isBadRequest());

        verify(authenticationService).register(any(AuthRegisterRequest.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrow400WhenRegisterCalledWithInvalidDto() throws Exception {

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(null, null, null);

        performPost("/api/auth/register", registerRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200AndLoginUser() throws Exception {

        AuthAuthenticateRequest authenticateRequest = AuthTestDataProvider.authAuthenticateRequest();

        AuthAuthenticateResponse authenticateResponse =
                new AuthAuthenticateResponse(AuthTestDataProvider.ACCESS_TOKEN, AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(authenticationService.login(any(AuthAuthenticateRequest.class))).thenReturn(authenticateResponse);

        performPost("/api/auth/authenticate", authenticateRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authenticateResponse.accessToken()))
                .andExpect(jsonPath("$.refreshToken").value(authenticateResponse.refreshToken()));

        verify(authenticationService).login(any(AuthAuthenticateRequest.class));
    }

    @Test
    void shouldThrow401WhenLoginCalledWithInvalidCredentials() throws Exception {

        AuthAuthenticateRequest authenticateRequest = AuthTestDataProvider.authAuthenticateRequest();

        doThrow(new BadCredentialsException("Invalid username or password."))
                .when(authenticationService).login(any(AuthAuthenticateRequest.class));

        performPost("/api/auth/authenticate", authenticateRequest)
                .andExpect(status().isUnauthorized());

        verify(authenticationService).login(any(AuthAuthenticateRequest.class));
    }

    @Test
    void shouldThrow400WhenLoginCalledWithInvalidDto() throws Exception {

        AuthAuthenticateRequest authenticateRequest =
                AuthTestDataProvider.authAuthenticateRequest(null, null);

        performPost("/api/auth/authenticate", authenticateRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200AndRefreshToken() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest();

        AuthAuthenticateResponse refreshTokenResponse =
                new AuthAuthenticateResponse(AuthTestDataProvider.ACCESS_TOKEN, refreshTokenRequest.token());

        when(authenticationService.refreshToken(any(AuthRefreshTokenRequest.class))).thenReturn(refreshTokenResponse);

        performPost("/api/auth/refresh", refreshTokenRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(refreshTokenResponse.accessToken()))
                .andExpect(jsonPath("$.refreshToken").value(refreshTokenResponse.refreshToken()));

        verify(authenticationService).refreshToken(any(AuthRefreshTokenRequest.class));
    }

    @Test
    void shouldThrow400WhenRefreshTokenCalledWithInvalidDto() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest =
                AuthTestDataProvider.authRefreshTokenRequest(null);

        performPost("/api/auth/refresh", refreshTokenRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn204AndLogoutUserSession() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest = AuthTestDataProvider.authRefreshTokenRequest();

        doNothing().when(authenticationService).logout(any(AuthRefreshTokenRequest.class));

        performPost("/api/auth/logout", refreshTokenRequest)
                .andExpect(status().isNoContent());

        verify(authenticationService).logout(any(AuthRefreshTokenRequest.class));
    }

    @Test
    void shouldThrow400WhenLogoutCalledWithInvalidDto() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest = AuthTestDataProvider.authRefreshTokenRequest(null);

        performPost("/api/auth/logout", refreshTokenRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrow404WhenLogoutCalledWithNonexistentRefreshToken() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest = AuthTestDataProvider.authRefreshTokenRequest();

        doThrow(new RefreshTokenNotFoundException("Refresh token not found"))
                .when(authenticationService).logout(any(AuthRefreshTokenRequest.class));

        performPost("/api/auth/logout", refreshTokenRequest)
                .andExpect(status().isNotFound());

        verify(authenticationService).logout(any(AuthRefreshTokenRequest.class));
    }

    @Test
    void shouldThrow401WhenLogoutCalledWithExpiredRefreshToken() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest = AuthTestDataProvider.authRefreshTokenRequest();

        doThrow(new RefreshTokenExpiredException("Refresh token expired"))
                .when(authenticationService).logout(any(AuthRefreshTokenRequest.class));

        performPost("/api/auth/logout", refreshTokenRequest)
                .andExpect(status().isUnauthorized());

        verify(authenticationService).logout(any(AuthRefreshTokenRequest.class));
    }

    @Test
    void shouldThrow401WhenLogoutCalledWithRevokedRefreshToken() throws Exception {

        AuthRefreshTokenRequest refreshTokenRequest = AuthTestDataProvider.authRefreshTokenRequest();

        doThrow(new RefreshTokenRevokedException("Refresh token revoked"))
                .when(authenticationService).logout(any(AuthRefreshTokenRequest.class));

        performPost("/api/auth/logout", refreshTokenRequest)
                .andExpect(status().isUnauthorized());

        verify(authenticationService).logout(any(AuthRefreshTokenRequest.class));
    }

    private ResultActions performPost(String url, Object body) throws Exception {

        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }
}
