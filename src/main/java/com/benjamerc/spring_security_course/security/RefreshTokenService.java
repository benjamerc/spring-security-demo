package com.benjamerc.spring_security_course.security;

import com.benjamerc.spring_security_course.config.security.JwtProperties;
import com.benjamerc.spring_security_course.domain.entity.RefreshToken;
import com.benjamerc.spring_security_course.domain.entity.User;
import com.benjamerc.spring_security_course.exception.token.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshToken createRefreshToken(User user, UUID session) {

        long expirationMillis = jwtProperties.getRefreshToken().getExpiration();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID())
                .expiryDate(Instant.now().plusMillis(expirationMillis))
                .session(session)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(UUID token) {

        RefreshToken refreshToken = getTokenOrThrow(token);

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(UUID token) {

        RefreshToken refreshToken = getTokenOrThrow(token);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken rotateRefreshToken(UUID token) {

        RefreshToken refreshToken = validateRefreshToken(token);

        User user = refreshToken.getUser();
        UUID session = refreshToken.getSession();

        revokeTokensBySession(user, session);

        return createRefreshToken(user, session);
    }

    public void revokeTokensBySession(User user, UUID session) {

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndSessionAndRevokedFalse(user, session);

        tokens.forEach(t -> t.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }

    public void revokeAllTokensForUser(User user) {

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        refreshTokens.forEach(t -> t.setRevoked(true));

        refreshTokenRepository.saveAll(refreshTokens);
    }

    public RefreshToken getTokenOrThrow(UUID token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found: " + token));
    }
}
