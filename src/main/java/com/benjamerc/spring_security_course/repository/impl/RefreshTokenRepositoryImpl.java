package com.benjamerc.spring_security_course.repository.impl;

import com.benjamerc.spring_security_course.repository.RefreshTokenRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public class RefreshTokenRepositoryImpl implements RefreshTokenRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int revokeIfNotRevoked(Long id, Long newId) {
        return entityManager.createQuery(
                        "UPDATE RefreshToken r SET r.revoked = true, r.replacedBy = :newId WHERE r.id = :id AND r.revoked = false")
                .setParameter("id", id)
                .setParameter("newId", newId)
                .executeUpdate();
    }

    @Override
    public int revokeByFamilyId(UUID familyId) {
        return entityManager.createQuery(
                        "UPDATE RefreshToken r SET r.revoked = true WHERE r.familyId = :familyId AND r.revoked = false")
                .setParameter("familyId", familyId)
                .executeUpdate();
    }

    @Override
    public int revokeAllByUserId(Long userId) {
        return entityManager.createQuery(
                        "UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
