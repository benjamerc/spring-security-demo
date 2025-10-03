package com.benjamerc.spring_security_course.users;

import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.model.User;

public class UserTestDataProvider {

    public static User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .username(role == Role.ADMIN ? "admin@email.com" : "user@email.com")
                .name(role == Role.ADMIN ? "admin" : "user")
                .password("pass123")
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
