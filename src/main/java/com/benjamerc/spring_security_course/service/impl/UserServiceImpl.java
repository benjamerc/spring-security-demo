package com.benjamerc.spring_security_course.service.impl;

import com.benjamerc.spring_security_course.domain.dto.user.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import com.benjamerc.spring_security_course.mapper.UserMapper;
import com.benjamerc.spring_security_course.repository.UserRepository;
import com.benjamerc.spring_security_course.security.CustomUserDetails;
import com.benjamerc.spring_security_course.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserProfileResponse userProfile(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return userMapper.toUserProfileResponse(user);
    }

    @Override
    public UserPartialUpdateResponse updateProfile(Authentication authentication, UserPartialUpdateRequest request) {

        User user = getAuthenticatedUser(authentication);

        Optional.ofNullable(request.username())
                .filter(u -> !u.isBlank())
                .ifPresent(user::setUsername);

        Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .ifPresent(user::setName);

        User updatedUser = userRepository.save(user);

        return new UserPartialUpdateResponse(
                updatedUser.getUsername(),
                updatedUser.getName()
        );
    }

    @Override
    public void deleteAccount(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        userRepository.delete(user);
    }

    private User getAuthenticatedUser(Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userDetails.getId()
                ));
    }
}
