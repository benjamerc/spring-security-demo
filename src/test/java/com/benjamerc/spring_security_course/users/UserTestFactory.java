package com.benjamerc.spring_security_course.users;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserTestFactory {

    public static User defineUser(String username, String name, String password, Role role, PasswordEncoder encoder) {

        return User.builder()
                .username(username)
                .name(name)
                .password(encoder.encode(password))
                .role(role)
                .build();
    }
}
