package com.benjamerc.spring_security_course.controller;

import com.benjamerc.spring_security_course.domain.dto.user.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import com.benjamerc.spring_security_course.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> userProfile(Authentication authentication) {

        return ResponseEntity.ok(userService.userProfile(authentication));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserPartialUpdateResponse> updateProfile(Authentication authentication,
                                                                   @RequestBody @Valid UserPartialUpdateRequest request) {

        return ResponseEntity.ok(userService.updateProfile(authentication, request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {

        userService.deleteAccount(authentication);

        return ResponseEntity.noContent().build();
    }
}
