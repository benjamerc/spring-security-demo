package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import com.benjamerc.spring_security_course.users.mapper.UserMapper;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
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
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDetails.getId()));
    }
}
