package com.benjamerc.spring_security_course.users.repository;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.model.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsername() {

        User user = createUserProfileTest();

        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername(user.getUsername());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(result.get().getName()).isEqualTo(user.getName());
        assertThat(result.get().getPassword()).isEqualTo(user.getPassword());
        assertThat(result.get().getRole()).isEqualTo(user.getRole());
    }

    private User createUserProfileTest() {

        return User.builder()
                .username("user@email.com")
                .name("user")
                .password("password")
                .role(Role.USER)
                .build();
    }
}
