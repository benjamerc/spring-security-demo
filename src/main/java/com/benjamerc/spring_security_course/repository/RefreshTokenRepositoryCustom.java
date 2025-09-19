package com.benjamerc.spring_security_course.repository;

import java.util.UUID;

public interface RefreshTokenRepositoryCustom {

    int revokeIfNotRevoked(Long id, Long newId);

    int revokeByFamilyId(UUID familyId);

    int revokeAllByUserId(Long userId);
}
