package com.benjamerc.spring_security_course.authentication.service;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.authentication.dto.token.RefreshTokenWithRaw;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenExpiredException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenRevokedException;
import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
import com.benjamerc.spring_security_course.authentication.mapper.AuthenticationMapper;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.core.AccessTokenService;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void shouldRegisterUser() {

        User user = UserTestDataProvider.user();

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(user.getUsername(), user.getName(), user.getPassword());

        User savedUser = UserTestDataProvider.user(1L);

        AuthRegisterResponse registerResponse =
                new AuthRegisterResponse(savedUser.getUsername(), savedUser.getName(), savedUser.getRole());

        when(passwordEncoder.encode(anyString())).thenReturn(savedUser.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationMapper.toAuthRegisterResponse(savedUser)).thenReturn(registerResponse);

        AuthRegisterResponse result = authenticationService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(registerResponse);

        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).save(any(User.class));
        verify(authenticationMapper).toAuthRegisterResponse(savedUser);
    }

    @Test
    void shouldThrowUsernameAlreadyExistsWhenRegisterCalledWithExistingUsername() {

        User user = UserTestDataProvider.user();

        AuthRegisterRequest registerRequest =
                AuthTestDataProvider.authRegisterRequest(user.getUsername(), user.getName(), user.getPassword());

        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByUsername(registerRequest.username());
    }

    @Test
    void shouldAuthenticateUser() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        Authentication authenticationMock = mock(Authentication.class);

        String accessToken = AuthTestDataProvider.ACCESS_TOKEN;
        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(userDetails.getUser());

        RefreshTokenWithRaw refreshTokenWithRaw = AuthTestDataProvider.refreshTokenWithRaw(refreshToken, refreshToken.getToken());

        AuthAuthenticateRequest authenticateRequest =
                AuthTestDataProvider.authAuthenticateRequest(userDetails.getUser().getUsername(), userDetails.getUser().getPassword());

        AuthAuthenticateResponse authenticateResponse =
                new AuthAuthenticateResponse(accessToken, refreshToken.getToken());

        when(authenticationMock.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(accessTokenService.createAccessToken(userDetails.getUser())).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(eq(userDetails.getUser()), any(UUID.class))).thenReturn(refreshTokenWithRaw);

        AuthAuthenticateResponse result = authenticationService.login(authenticateRequest);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(authenticateResponse);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(accessTokenService).createAccessToken(userDetails.getUser());
        verify(refreshTokenService).createRefreshToken(eq(userDetails.getUser()), any(UUID.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginCalledWithInvalidCredentials() {

        AuthAuthenticateRequest request = AuthTestDataProvider.authAuthenticateRequest();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginCalledWithNonexistentUser() {

        AuthAuthenticateRequest request = AuthTestDataProvider.authAuthenticateRequest();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found with username: " + request.username()));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: " + request.username());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldRefreshToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        User user = UserTestDataProvider.user();
        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);
        RefreshTokenWithRaw newRefreshToken =
                AuthTestDataProvider.refreshTokenWithRaw(refreshToken, refreshToken.getToken());

        String newAccessToken = AuthTestDataProvider.ACCESS_TOKEN;

        AuthAuthenticateResponse response =
                new AuthAuthenticateResponse(newAccessToken, newRefreshToken.getRawToken());

        when(refreshTokenService.rotateRefreshToken(request.token())).thenReturn(newRefreshToken);
        when(accessTokenService.createAccessToken(newRefreshToken.getRefreshToken().getUser())).thenReturn(newAccessToken);

        AuthAuthenticateResponse result = authenticationService.refreshToken(request);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(response);

        verify(refreshTokenService).rotateRefreshToken(request.token());
        verify(accessTokenService).createAccessToken(newRefreshToken.getRefreshToken().getUser());
    }

    @Test
    void shouldThrowWhenRefreshTokenCalledWithRevokedToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.rotateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenRevokedException("Refresh token revoked"));

        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(RefreshTokenRevokedException.class)
                .hasMessage("Refresh token revoked");

        verify(refreshTokenService).rotateRefreshToken(request.token());
        verify(accessTokenService, never()).createAccessToken(any());
    }

    @Test
    void shouldThrowWhenRefreshTokenCalledWithExpiredToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.rotateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenExpiredException("Refresh token expired"));

        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenService).rotateRefreshToken(request.token());
        verify(accessTokenService, never()).createAccessToken(any());
    }

    @Test
    void shouldThrowWhenRefreshTokenCalledWithNonexistentToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.rotateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenNotFoundException("Refresh token not found"));

        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenService).rotateRefreshToken(request.token());
        verify(accessTokenService, never()).createAccessToken(any());
    }

    @Test
    void shouldLogoutUser() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        User user = UserTestDataProvider.user();
        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);

        when(refreshTokenService.validateRefreshToken(request.token())).thenReturn(refreshToken);
        doNothing().when(refreshTokenService).revokeTokensBySession(refreshToken.getUser(), refreshToken.getSession());

        authenticationService.logout(request);

        verify(refreshTokenService).validateRefreshToken(request.token());
        verify(refreshTokenService).revokeTokensBySession(refreshToken.getUser(), refreshToken.getSession());
    }

    @Test
    void shouldThrowWhenLogoutCalledWithRevokedToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.validateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenRevokedException("Refresh token revoked"));

        assertThatThrownBy(() -> authenticationService.logout(request))
                .isInstanceOf(RefreshTokenRevokedException.class)
                .hasMessage("Refresh token revoked");

        verify(refreshTokenService).validateRefreshToken(request.token());
    }

    @Test
    void shouldThrowWhenLogoutCalledWithExpiredToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.validateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenExpiredException("Refresh token expired"));

        assertThatThrownBy(() -> authenticationService.logout(request))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenService).validateRefreshToken(request.token());
    }

    @Test
    void shouldThrowWhenLogoutCalledWithNonexistentToken() {

        AuthRefreshTokenRequest request =
                AuthTestDataProvider.authRefreshTokenRequest(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        when(refreshTokenService.validateRefreshToken(request.token()))
                .thenThrow(new RefreshTokenNotFoundException("Refresh token not found"));

        assertThatThrownBy(() -> authenticationService.logout(request))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenService).validateRefreshToken(request.token());
    }
}
