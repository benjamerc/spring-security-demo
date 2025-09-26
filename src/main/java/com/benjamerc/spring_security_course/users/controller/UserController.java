package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> userProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(userService.userProfile(userDetails));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserPartialUpdateResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @RequestBody @Valid UserPartialUpdateRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userDetails, request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.deleteAccount(userDetails);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/logout-all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.logoutAll(userDetails);

        return ResponseEntity.noContent().build();
    }
}
