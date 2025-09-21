package com.benjamerc.spring_security_course.service.impl;

import com.benjamerc.spring_security_course.domain.dto.user.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import com.benjamerc.spring_security_course.mapper.UserMapper;
import com.benjamerc.spring_security_course.repository.UserRepository;
import com.benjamerc.spring_security_course.security.CustomUserDetails;
import com.benjamerc.spring_security_course.security.RefreshTokenService;
import com.benjamerc.spring_security_course.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserProfileResponse userProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getUserOrThrow(userDetails);

        return userMapper.toUserProfileResponse(user);
    }

    @Override
    public UserPartialUpdateResponse updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, UserPartialUpdateRequest request) {

        User user = getUserOrThrow(userDetails);

        Optional.ofNullable(request.username())
                .filter(u -> !u.isBlank())
                .ifPresent(user::setUsername);

        Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .ifPresent(user::setName);

        return userMapper.toUserPartialUpdateResponse(userRepository.save(user));
    }

    @Override
    public void deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getUserOrThrow(userDetails);

        userRepository.delete(user);
    }

    @Override
    public void logoutAll(CustomUserDetails userDetails) {

        User user = getUserOrThrow(userDetails);

        refreshTokenService.revokeAllTokensForUser(user);
    }


    private User getUserOrThrow(CustomUserDetails userDetails) {

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userDetails.getId()));
    }
}
