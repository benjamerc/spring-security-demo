package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.mapper.UserMapper;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnUserProfileResponse() {

        User user = User.builder()
                .id(1L)
                .username("user@email.com")
                .name("user")
                .password("password")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserProfileResponse response = new UserProfileResponse("user@email.com", "user");

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponse(user)).thenReturn(response);

        UserProfileResponse result = userService.userProfile(userDetails);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("user@email.com");
        assertThat(result.name()).isEqualTo("user");
    }

    @Test
    void shouldPartialUpdateUserProfile() {

        User user = User.builder()
                .id(1L)
                .username("user@email.com")
                .name("user")
                .password("password")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserPartialUpdateRequest request = new UserPartialUpdateRequest("", "updated");

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toUserPartialUpdateResponse(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    return new UserPartialUpdateResponse(savedUser.getUsername(), savedUser.getName());
                });

        UserPartialUpdateResponse result = userService.updateProfile(userDetails, request);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("user@email.com");
        assertThat(result.name()).isEqualTo("updated");
    }

    @Test
    void shouldDeleteUserAccount() {

        User user = User.builder()
                .id(1L)
                .username("user@email.com")
                .name("user")
                .password("password")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));

        userService.deleteAccount(userDetails);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void shouldRevokeAllRefreshTokensForUser() {

        User user = User.builder()
                .id(1L)
                .username("user@email.com")
                .name("user")
                .password("password")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));

        userService.logoutAll(userDetails);

        verify(refreshTokenService, times(1)).revokeAllTokensForUser(user);
    }
}
