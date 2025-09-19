package com.benjamerc.spring_security_course.repository;

import com.benjamerc.spring_security_course.domain.entity.RefreshToken;
import com.benjamerc.spring_security_course.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    void deleteByUser(User user);

    void deleteByExpiryDateBefore(java.time.Instant now);
}
