package com.benjamerc.spring_security_course.security;

import com.benjamerc.spring_security_course.config.security.JwtProperties;
import com.benjamerc.spring_security_course.domain.entity.RefreshToken;
import com.benjamerc.spring_security_course.domain.entity.User;
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

    public RefreshToken createRefreshToken(User user) {

        Long expirationMillis = jwtProperties.getRefreshToken().getExpiration();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(expirationMillis))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = getTokenOrThrow(token);

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken = getTokenOrThrow(token);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllTokensForUser(User user) {

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        refreshTokens.forEach(t -> t.setRevoked(true));

        refreshTokenRepository.saveAll(refreshTokens);
    }

    public void deleteExpiredTokens() {

        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }

    private RefreshToken getTokenOrThrow(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }
}
