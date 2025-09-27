package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import com.benjamerc.spring_security_course.users.mapper.UserMapper;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnUserProfile() {

        User user = UserTestDataProvider.user(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserProfileResponse profileResponse = new UserProfileResponse(user.getUsername(), user.getName());

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponse(user)).thenReturn(profileResponse);

        UserProfileResponse result = userService.userProfile(userDetails);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.username()).isEqualTo(user.getUsername());

        verify(userRepository).findById(userDetails.getId());
        verify(userMapper).toUserProfileResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundWhenUserProfileCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = new CustomUserDetails(UserTestDataProvider.user(99L));

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.userProfile(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getId());

        verify(userRepository).findById(userDetails.getId());
    }

    @Test
    void shouldUpdateUserProfile() {

        User user = UserTestDataProvider.user(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();
        UserPartialUpdateResponse updatedResponse = new UserPartialUpdateResponse(user.getUsername(), updateRequest.name());

        User updatedUser = UserTestDataProvider.user(1L);
        updatedUser.setName(updateRequest.name());

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserPartialUpdateResponse(any(User.class))).thenReturn(updatedResponse);

        UserPartialUpdateResponse result = userService.updateProfile(userDetails, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(updatedResponse.username());
        assertThat(result.name()).isEqualTo(updatedResponse.name());

        verify(userRepository).findById(userDetails.getId());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserPartialUpdateResponse(any(User.class));
    }

    @Test
    void shouldNotUpdateValueInUserProfileWhenRequestContainsBlankValue() {

        User user = UserTestDataProvider.user(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequestWithoutValues();
        UserPartialUpdateResponse updatedResponse = new UserPartialUpdateResponse(user.getUsername(), user.getName());

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserPartialUpdateResponse(any(User.class))).thenReturn(updatedResponse);

        UserPartialUpdateResponse result = userService.updateProfile(userDetails, updateRequest);

        assertThat(result.username()).isEqualTo(updatedResponse.username());
        assertThat(result.name()).isEqualTo(updatedResponse.name());

        verify(userRepository).findById(userDetails.getId());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserPartialUpdateResponse(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdateProfileCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = new CustomUserDetails(UserTestDataProvider.user(99L));
        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userDetails, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getId());

        verify(userRepository).findById(userDetails.getId());
    }

    @Test
    void shouldDeleteUserAccount() {

        User user = UserTestDataProvider.user(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(eq(user));

        userService.deleteAccount(userDetails);

        verify(userRepository).findById(userDetails.getId());
        verify(userRepository).delete(eq(user));
    }

    @Test
    void shouldThrowUserNotFoundWhenDeleteAccountCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = new CustomUserDetails(UserTestDataProvider.user(99L));

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getId());

        verify(userRepository).findById(userDetails.getId());
    }

    @Test
    void shouldLogoutAllUserAccounts() {

        User user = UserTestDataProvider.user(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenService).revokeAllTokensForUser(eq(user));

        userService.logoutAll(userDetails);

        verify(userRepository).findById(userDetails.getId());
        verify(refreshTokenService).revokeAllTokensForUser(eq(user));
    }

    @Test
    void shouldThrowUserNotFoundWhenLogoutAllCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = new CustomUserDetails(UserTestDataProvider.user(99L));

        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.logoutAll(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getId());

        verify(userRepository).findById(userDetails.getId());
    }
}
