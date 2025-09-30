package com.benjamerc.spring_security_course.users;

import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.model.User;

public class UserTestDataProvider {

    public static User user() {

        return User.builder()
                .username("user@email.com")
                .name("user")
                .password("pass123")
                .role(Role.USER)
                .build();
    }

    public static User user(Long id) {

        return User.builder()
                .id(id)
                .username("user@email.com")
                .name("user")
                .password("pass123")
                .role(Role.USER)
                .build();
    }

    public static AdminUserUpdateRequest adminUserUpdateRequest() {

        return new AdminUserUpdateRequest("updated@email.com", "", null);
    }

    public static AdminUserUpdateRequest adminUserUpdateRequest(String username, String name, Role role) {

        return new AdminUserUpdateRequest(username, name, role);
    }

    public static UserPartialUpdateRequest userPartialUpdateRequest() {

        return new UserPartialUpdateRequest("", "updated");
    }

    public static UserPartialUpdateRequest userPartialUpdateRequest(String username, String name) {

        return new UserPartialUpdateRequest(username, name);
    }
}
