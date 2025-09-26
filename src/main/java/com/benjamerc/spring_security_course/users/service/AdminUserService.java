package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserResponse;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserSummaryResponse> getAllUsers(Pageable pageable);

    AdminUserResponse getUserById(Long id);

    AdminUserResponse partialUpdate(Long id, AdminUserUpdateRequest request);

    void deleteUser(Long id);

    void logoutAll(Long id);
}
