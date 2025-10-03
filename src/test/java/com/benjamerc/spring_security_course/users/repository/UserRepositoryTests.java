package com.benjamerc.spring_security_course.users.repository;

import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnUserByUsername() {

        User user = UserTestDataProvider.user(null);

        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername(user.getUsername());

        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void shouldReturnEmptyOptionalWhenUsernameNotExist() {

        Optional<User> result = userRepository.findByUsername("inexistent@username.com");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowDataIntegrityViolationWhenUsernameAlreadyExist() {

        User user1 = UserTestDataProvider.user(null);
        userRepository.save(user1);

        User user2 = UserTestDataProvider.user(null);

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {

        User user = UserTestDataProvider.user(null);
        userRepository.save(user);

        boolean result = userRepository.existsByUsername(user.getUsername());

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {

        boolean result = userRepository.existsByUsername("inexistent@username.com");

        assertThat(result).isFalse();
    }
}
