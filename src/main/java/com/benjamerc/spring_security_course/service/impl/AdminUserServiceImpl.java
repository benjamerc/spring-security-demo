package com.benjamerc.spring_security_course.service.impl;

import com.benjamerc.spring_security_course.domain.dto.admin.user.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserResponse;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import com.benjamerc.spring_security_course.mapper.AdminUserMapper;
import com.benjamerc.spring_security_course.repository.UserRepository;
import com.benjamerc.spring_security_course.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.Role;
import com.benjamerc.spring_security_course.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminUserMapper adminUserMapper;
    private final RefreshTokenService refreshTokenService;

    @Value("${pagination.max-page-size}")
    private int maxPageSize;

    @Override
    public Page<AdminUserSummaryResponse> getAllUsers(Pageable pageable) {

        Pageable safePageable = getSafePageable(pageable);

        return userRepository.findAll(safePageable)
                .map(adminUserMapper::toAdminUserSummaryResponse);
    }

    @Override
    public AdminUserResponse getUserById(Long id) {

        User user = getUserByIdOrThrow(id);

        return adminUserMapper.toAdminUserResponse(user);
    }

    @Override
    public AdminUserResponse partialUpdate(Long id, AdminUserUpdateRequest request) {

        User user = getUserByIdOrThrow(id);

        Optional.ofNullable(request.username())
                .filter(u -> !u.isBlank())
                .ifPresent(user::setUsername);

        Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .ifPresent(user::setName);

        Optional.ofNullable(request.role())
                .filter(r -> r != Role.ADMIN)
                .ifPresent(user::setRole);

        return adminUserMapper.toAdminUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {

        User user = getUserByIdOrThrow(id);

        userRepository.delete(user);
    }

    @Override
    public void logoutAll(Long id) {

        User user = getUserByIdOrThrow(id);

        refreshTokenService.revokeAllTokensForUser(user);
    }

    private User getUserByIdOrThrow(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private Pageable getSafePageable(Pageable pageable) {

        int size = Math.min(pageable.getPageSize(), maxPageSize);

        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
