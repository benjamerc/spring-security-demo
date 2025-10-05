package com.benjamerc.spring_security_course.authentication.security;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.dto.token.RefreshTokenWithRaw;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenExpiredException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenRevokedException;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.authentication.repository.RefreshTokenRepository;
import com.benjamerc.spring_security_course.security.config.JwtProperties;
import com.benjamerc.spring_security_course.security.core.TokenUtils;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldCreateRefreshToken() {

        User user = UserTestDataProvider.user(1L);

        RefreshToken savedRefreshToken = AuthTestDataProvider.refreshToken(user);

        JwtProperties.RefreshToken refreshTokenProperties = mock(JwtProperties.RefreshToken.class);
        long expiration = AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION;

        RefreshTokenWithRaw response = new RefreshTokenWithRaw(savedRefreshToken, savedRefreshToken.getToken());

        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        when(refreshTokenProperties.getExpiration()).thenReturn(expiration);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

        RefreshTokenWithRaw result = refreshTokenService.createRefreshToken(user, AuthTestDataProvider.REFRESH_TOKEN_SESSION);

        assertThat(result).isNotNull();
        assertThat(result.getRawToken()).isNotBlank();
        assertThat(result.getRefreshToken().getToken()).isNotBlank();
        assertThat(result.getRefreshToken().getExpiryDate()).isAfter(Instant.now());
        assertThat(result.getRefreshToken().getUser()).isEqualTo(user);

        verify(jwtProperties).getRefreshToken();
        verify(refreshTokenProperties).getExpiration();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldValidateRefreshToken() {

        String rawToken = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        User user = UserTestDataProvider.user(1L);
        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(rawToken))).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.validateRefreshToken(rawToken);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
        assertThat(result.getUser()).isEqualTo(user);

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(rawToken));
    }

    @Test
    void shouldThrowWhenValidateRefreshTokenCalledWithRevokedToken() {

        String rawToken = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));

        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(rawToken))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                .isInstanceOf(RefreshTokenRevokedException.class)
                .hasMessage("Refresh token revoked");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(rawToken));
    }

    @Test
    void shouldThrowWhenValidateRefreshTokenCalledWithExpiredToken() {

        String rawToken = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));

        refreshToken.setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(rawToken))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(rawToken));
    }

    @Test
    void shouldThrowWhenValidateRefreshTokenCalledWithNonexistentToken() {

        String rawToken = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(rawToken))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(rawToken));
    }

    @Test
    void shouldRevokeRefreshTokenByToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken notRevokedToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));

        RefreshToken revokedToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));
        revokedToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(notRevokedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(revokedToken);

        refreshTokenService.revokeRefreshToken(token);

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowWhenRevokeRefreshTokenCalledWithRevokedToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));
        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken(token))
                .isInstanceOf(RefreshTokenRevokedException.class)
                .hasMessage("Refresh token revoked");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldThrowWhenRevokeRefreshTokenCalledWithExpiredToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));
        refreshToken.setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken(token))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldThrowWhenRevokeRefreshTokenCalledWithNonexistentToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken(token))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldRotateRefreshToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;
        User user = UserTestDataProvider.user(1L);
        RefreshToken oldRefreshToken = AuthTestDataProvider.refreshToken(user);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(oldRefreshToken));

        List<RefreshToken> oldTokens = List.of(oldRefreshToken);
        RefreshToken revokedToken = AuthTestDataProvider.refreshToken(user);
        revokedToken.setRevoked(true);
        List<RefreshToken> revokedTokens = List.of(revokedToken);

        when(refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(oldRefreshToken.getUser(), oldRefreshToken.getSession()))
                .thenReturn(oldTokens);
        when(refreshTokenRepository.saveAll(anyList())).thenReturn(revokedTokens);

        JwtProperties.RefreshToken refreshTokenProperties = mock(JwtProperties.RefreshToken.class);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        when(refreshTokenProperties.getExpiration()).thenReturn(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION);

        RefreshToken savedRefreshToken = AuthTestDataProvider.refreshToken(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

        RefreshTokenWithRaw result = refreshTokenService.rotateRefreshToken(token);

        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken().getToken()).isNotBlank();
        assertThat(result.getRefreshToken().getSession()).isNotNull();
        assertThat(result.getRefreshToken().isRevoked()).isFalse();

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
        verify(refreshTokenRepository).findAllByUserAndSessionAndRevokedFalse(oldRefreshToken.getUser(), oldRefreshToken.getSession());
        verify(refreshTokenRepository).saveAll(anyList());
        verify(jwtProperties).getRefreshToken();
        verify(refreshTokenProperties).getExpiration();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }


    @Test
    void shouldThrowWhenRotateRefreshTokenCalledWithRevokedToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));

        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(token))
                .isInstanceOf(RefreshTokenRevokedException.class)
                .hasMessage("Refresh token revoked");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldThrowWhenRotateRefreshTokenCalledWithExpiredToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(UserTestDataProvider.user(1L));

        refreshToken.setExpiryDate(Instant.now().minusMillis(AuthTestDataProvider.REFRESH_TOKEN_EXPIRATION));

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(token))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldThrowWhenRotateRefreshTokenCalledWithNonexistentToken() {

        String token = AuthTestDataProvider.REFRESH_TOKEN_STRING;

        when(refreshTokenRepository.findByToken(TokenUtils.hashSHA256(token))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(token))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenRepository).findByToken(TokenUtils.hashSHA256(token));
    }

    @Test
    void shouldRevokeTokensBySession() {

        User user = UserTestDataProvider.user(1L);
        UUID session = AuthTestDataProvider.REFRESH_TOKEN_SESSION;

        RefreshToken refreshToken1 = AuthTestDataProvider.refreshToken(user);
        RefreshToken refreshToken2 = AuthTestDataProvider.refreshToken(user);

        when(refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, session))
                .thenReturn(List.of(refreshToken1, refreshToken2));

        refreshTokenService.revokeTokensBySession(user, session);

        verify(refreshTokenRepository).findAllByUserAndSessionAndRevokedFalse(user, session);

        verify(refreshTokenRepository).saveAll(argThat(iterable -> {

            List<RefreshToken> list = new ArrayList<>();
            iterable.forEach(list::add);

            return list.size() == 2 && list.stream().allMatch(RefreshToken::isRevoked);
        }));
    }

    @Test
    void shouldNotRevokedTokensBySessionWhenAreNotTokens() {

        User user = UserTestDataProvider.user(1L);
        UUID session = AuthTestDataProvider.REFRESH_TOKEN_SESSION;

        when(refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, session))
                .thenReturn(List.of());

        refreshTokenService.revokeTokensBySession(user, session);

        verify(refreshTokenRepository).findAllByUserAndSessionAndRevokedFalse(user, session);
        verify(refreshTokenRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRevokeAllTokensForUser() {

        User user = UserTestDataProvider.user(1L);

        RefreshToken refreshToken1 = AuthTestDataProvider.refreshToken(user);
        RefreshToken refreshToken2 = AuthTestDataProvider.refreshToken(user);

        when(refreshTokenRepository.findAllByUserAndRevokedFalse(user)).thenReturn(List.of(refreshToken1, refreshToken2));

        refreshTokenService.revokeAllTokensForUser(user);

        verify(refreshTokenRepository).findAllByUserAndRevokedFalse(user);

        verify(refreshTokenRepository).saveAll(argThat(iterable -> {

            List<RefreshToken> list = new ArrayList<>();
            iterable.forEach(list::add);

            return list.size() == 2 && list.stream().allMatch(RefreshToken::isRevoked);
        }));
    }

    @Test
    void shouldNotRevokedTokensByUserWhenAreNotTokens() {

        User user = UserTestDataProvider.user(1L);

        when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
                .thenReturn(List.of());

        refreshTokenService.revokeAllTokensForUser(user);

        verify(refreshTokenRepository).findAllByUserAndRevokedFalse(user);
        verify(refreshTokenRepository, never()).saveAll(anyList());
    }
}
