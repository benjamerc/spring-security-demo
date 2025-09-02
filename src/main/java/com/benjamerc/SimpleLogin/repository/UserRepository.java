package com.benjamerc.SimpleLogin.repository;

import com.benjamerc.SimpleLogin.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String username);

    boolean existsByEmail(String email);
}
