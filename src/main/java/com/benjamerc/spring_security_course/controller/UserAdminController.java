package com.benjamerc.spring_security_course.controller;

import com.benjamerc.spring_security_course.domain.dto.admin.user.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserResponse;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserAdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserSummaryResponse>> getAllUsers(
            @PageableDefault(page = 0, size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable("id") Long id) {

        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> partialUpdate(@PathVariable("id") Long id,
                                                           @RequestBody @Valid AdminUserUpdateRequest request) {

        return ResponseEntity.ok(adminUserService.partialUpdate(id, request));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        adminUserService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
