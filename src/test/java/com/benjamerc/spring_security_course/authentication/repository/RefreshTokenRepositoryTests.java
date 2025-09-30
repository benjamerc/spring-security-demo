package com.benjamerc.spring_security_course.authentication.repository;

import com.benjamerc.spring_security_course.authentication.AuthTestDataProvider;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RefreshTokenRepositoryTests {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnRefreshTokenByToken() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);
        refreshTokenRepository.save(refreshToken);

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(refreshToken.getToken());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(refreshToken);
    }

    @Test
    void shouldReturnEmptyOptionalForNonexistentToken() {

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(AuthTestDataProvider.REFRESH_TOKEN_STRING);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnActiveRefreshTokensByUser() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        assertThat(result).containsExactly(refreshToken);
    }

    @Test
    void shouldReturnActiveRefreshTokensByUserWithMultipleTokens() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        RefreshToken token1 = AuthTestDataProvider.refreshToken(user);
        RefreshToken token2 = AuthTestDataProvider.refreshToken(user);
        token2.setToken("31aed1ca-8bf5-405e-8cc5-bde4bbe91210");

        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        assertThat(result).containsExactly(token1, token2);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoActiveRefreshToken() {

        User nonExistentUser = UserTestDataProvider.user();
        userRepository.save(nonExistentUser);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndRevokedFalse(nonExistentUser);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnActiveRefreshTokensByUserAndSession() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, refreshToken.getSession());

        assertThat(result).containsExactly(refreshToken);
    }

    @Test
    void shouldReturnActiveRefreshTokensByUserAndSessionWithMultipleTokens() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        UUID sessionId = AuthTestDataProvider.REFRESH_TOKEN_SESSION;

        RefreshToken token1 = AuthTestDataProvider.refreshToken(user);

        RefreshToken token2 = AuthTestDataProvider.refreshToken(user);
        token2.setToken("31aed1ca-8bf5-405e-8cc5-bde4bbe91210");
        token2.setSession(sessionId);

        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, sessionId);

        assertThat(result).containsExactly(token1, token2);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoActiveTokensInSession() {

        User user = UserTestDataProvider.user();
        userRepository.save(user);

        RefreshToken refreshToken = AuthTestDataProvider.refreshToken(user);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> result = refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, refreshToken.getSession());

        assertThat(result).isEmpty();
    }
}
