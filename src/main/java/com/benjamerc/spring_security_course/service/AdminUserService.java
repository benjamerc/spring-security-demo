package com.benjamerc.spring_security_course.service;

import com.benjamerc.spring_security_course.domain.dto.admin.user.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserResponse;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserSummaryResponse> getAllUsers(Pageable pageable);

    AdminUserResponse getUserById(Long id);

    AdminUserResponse partialUpdate(Long id, AdminUserUpdateRequest request);

    void deleteUser(Long id);

    void logoutAll(Long id);
}
