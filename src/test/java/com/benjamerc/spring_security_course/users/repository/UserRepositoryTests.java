package com.benjamerc.spring_security_course.users.repository;

import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnUserByUsername() {

        User user = UserTestDataProvider.user();

        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername(user.getUsername());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(result.get().getName()).isEqualTo(user.getName());
        assertThat(result.get().getPassword()).isEqualTo(user.getPassword());
        assertThat(result.get().getRole()).isEqualTo(user.getRole());
    }
}
