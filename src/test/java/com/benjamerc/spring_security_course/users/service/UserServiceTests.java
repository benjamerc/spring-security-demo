package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
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

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserProfileResponse profileResponse =
                new UserProfileResponse(userDetails.getUsername(), userDetails.getUser().getName());

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        when(userMapper.toUserProfileResponse(userDetails.getUser())).thenReturn(profileResponse);

        UserProfileResponse result = userService.userProfile(userDetails);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(userDetails.getUser().getName());
        assertThat(result.username()).isEqualTo(userDetails.getUsername());

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(userMapper).toUserProfileResponse(userDetails.getUser());
    }

    @Test
    void shouldThrowUserNotFoundWhenUserProfileCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.userProfile(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getUser().getId());

        verify(userRepository).findById(userDetails.getUser().getId());
    }

    @Test
    void shouldUpdateUserProfile() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();
        UserPartialUpdateResponse updatedResponse = new UserPartialUpdateResponse(userDetails.getUsername(), updateRequest.name());

        User updatedUser = UserTestDataProvider.user(1L);
        updatedUser.setName(updateRequest.name());

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserPartialUpdateResponse(any(User.class))).thenReturn(updatedResponse);

        UserPartialUpdateResponse result = userService.updateProfile(userDetails, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(updatedResponse.username());
        assertThat(result.name()).isEqualTo(updatedResponse.name());

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserPartialUpdateResponse(any(User.class));
    }

    @Test
    void shouldNotUpdateValueInUserProfileWhenRequestContainsBlankValue() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest("", "");
        UserPartialUpdateResponse updatedResponse = new UserPartialUpdateResponse(userDetails.getUsername(), userDetails.getUser().getName());

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        when(userRepository.save(any(User.class))).thenReturn(userDetails.getUser());
        when(userMapper.toUserPartialUpdateResponse(any(User.class))).thenReturn(updatedResponse);

        UserPartialUpdateResponse result = userService.updateProfile(userDetails, updateRequest);

        assertThat(result.username()).isEqualTo(updatedResponse.username());
        assertThat(result.name()).isEqualTo(updatedResponse.name());

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserPartialUpdateResponse(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdateProfileCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);
        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userDetails, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getUser().getId());

        verify(userRepository).findById(userDetails.getUser().getId());
    }

    @Test
    void shouldThrowUsernameAlreadyExistsWhenUpdateProfileCalledWithExistingUsername() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);
        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        when(userRepository.existsByUsername(updateRequest.username()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(userDetails, updateRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUserAccount() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        doNothing().when(userRepository).delete(eq(userDetails.getUser()));

        userService.deleteAccount(userDetails);

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(userRepository).delete(eq(userDetails.getUser()));
    }

    @Test
    void shouldThrowUserNotFoundWhenDeleteAccountCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getUser().getId());

        verify(userRepository).findById(userDetails.getUser().getId());
    }

    @Test
    void shouldLogoutAllUserAccounts() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.of(userDetails.getUser()));
        doNothing().when(refreshTokenService).revokeAllTokensForUser(eq(userDetails.getUser()));

        userService.logoutAll(userDetails);

        verify(userRepository).findById(userDetails.getUser().getId());
        verify(refreshTokenService).revokeAllTokensForUser(eq(userDetails.getUser()));
    }

    @Test
    void shouldThrowUserNotFoundWhenLogoutAllCalledWithNonexistentUserId() {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        when(userRepository.findById(userDetails.getUser().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.logoutAll(userDetails))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userDetails.getUser().getId());

        verify(userRepository).findById(userDetails.getUser().getId());
    }
}
