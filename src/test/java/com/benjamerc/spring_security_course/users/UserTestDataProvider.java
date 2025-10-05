package com.benjamerc.spring_security_course.users;

import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.model.User;

public class UserTestDataProvider {

    public static final String ADMIN_USERNAME = "admin@email.com";
    public static final String ADMIN_NAME = "admin";
    public static final String USER_USERNAME = "user@email.com";
    public static final String USER_NAME = "user";
    public static final String PASSWORD = "pass123";

    public static User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .username(role == Role.ADMIN ? ADMIN_USERNAME : USER_USERNAME)
                .name(role == Role.ADMIN ? ADMIN_NAME : USER_NAME)
                .password(PASSWORD)
                .role(role)
                .build();
    }

    public static User user(Long id) {
        return user(id, Role.USER);
    }

    public static User admin(Long id) {
        return user(id, Role.ADMIN);
    }

    public static CustomUserDetails testUser(Long id) {

        return new CustomUserDetails(UserTestDataProvider.user(id));
    }

    public static CustomUserDetails testAdmin(Long id) {

        return new CustomUserDetails(UserTestDataProvider.admin(id));
    }

    public static AdminUserUpdateRequest adminUserUpdateRequest() {

        return new AdminUserUpdateRequest("updated@email.com", null, null);
    }

    public static AdminUserUpdateRequest adminUserUpdateRequest(String username, String name, Role role) {

        return new AdminUserUpdateRequest(username, name, role);
    }

    public static UserPartialUpdateRequest userPartialUpdateRequest() {

        return new UserPartialUpdateRequest(null, "updated");
    }

    public static UserPartialUpdateRequest userPartialUpdateRequest(String username, String name) {

        return new UserPartialUpdateRequest(username, name);
    }
}
